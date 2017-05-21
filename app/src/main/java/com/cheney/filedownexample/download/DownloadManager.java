package com.cheney.filedownexample.download;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.cheney.filedownexample.constant.Constant;
import com.cheney.filedownexample.db.DownloadDbHelper;
import com.cheney.filedownexample.db.TaskDBHandler;
import com.cheney.filedownexample.listener.DownloadListener;
import com.cheney.filedownexample.utils.FileUtils;
import com.cheney.filedownexample.utils.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by cheney on 2017/5/20.
 */

public class DownloadManager {
    private static DownloadManager instance;
    private int threads;
    private Executor taskExecutor;
    private Executor daoExecutor = Executors.newSingleThreadExecutor(new DownloadThreadFactory());
    private String rootPath;
    private Map<String, Set<DownloadListener>> listenerMap = new HashMap<>();
    private List<DownloadWorker> workers = new ArrayList<>();
    private BlockingQueue<DownloadTask> waitingQueue = new LinkedBlockingDeque<>();
    private Queue<DownloadTask> workingQueue = new ConcurrentLinkedQueue<>();
    private Queue<DownloadTask> idleQueue = new ConcurrentLinkedQueue<>();
    private TaskDBHandler downloadTaskDBHandler;
    private Context context;

    private DownloadManager() {

    }

    public synchronized static DownloadManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("DownloadManager instance is not set!");
        }
        return instance;
    }

    public synchronized static void setInstance(DownloadManager downloadManager) {
        instance = downloadManager;
    }

    DownloadTask takeFromWaitingQueue() throws InterruptedException {
        return waitingQueue.take();
    }

    private void moveTaskToIdleQueue(DownloadTask task) {
        workingQueue.remove(task);
        waitingQueue.remove(task);
        task.setStatus(DownloadTask.Status.IDLE);
        idleQueue.offer(task);
        downloadTaskDBHandler.updateTask(task);
    }

    private void moveTaskFromIdleToWaitingQueue(DownloadTask task) {
        idleQueue.remove(task);
        task.setStatus(DownloadTask.Status.WAITING);
        waitingQueue.offer(task);
        downloadTaskDBHandler.updateTask(task);
    }

    public void addRetryTask(DownloadTask task) {
        workingQueue.remove(task);
        task.setStatus(DownloadTask.Status.WAITING);
        waitingQueue.offer(task);
        downloadTaskDBHandler.updateTask(task);
    }

    public List<DownloadTask> getAllTasksInDb() {
        return downloadTaskDBHandler.getAllTasks();
    }

    public synchronized void addListener(String url, DownloadListener listener) {
        if (listener == null) {
            return;
        }
        Set<DownloadListener> list = listenerMap.get(url);
        if (list == null) {
            list = new HashSet<>();
            listenerMap.put(url, list);
        }
        list.add(listener);
    }

    public synchronized void removeListener(String url, DownloadListener listener) {
        if (listener == null) {
            return;
        }
        Set<DownloadListener> list = listenerMap.get(url);
        if (list != null) {
            list.remove(listener);
        }
    }

    public synchronized void removeListener(DownloadListener listener) {
        if (listener == null) {
            return;
        }
        Collection<Set<DownloadListener>> collection = listenerMap.values();
        if (collection != null) {
            for (Set<DownloadListener> listenerSet : collection) {
                if (listenerSet != null) {
                    listenerSet.remove(listener);
                }
            }
        }
    }

    private synchronized void removeListeners(String url) {
        listenerMap.remove(url);
    }

    private DownloadTask findTaskFromQueue(Queue<DownloadTask> queue, DownloadTask dstTask) {
        for (DownloadTask task : queue) {
            if (task.equals(dstTask)) {
                return task;
            }
        }
        return null;
    }

    private DownloadTask findTaskInAllQueues(DownloadTask dstTask) {
        DownloadTask foundTask = findTaskFromQueue(waitingQueue, dstTask);
        if (foundTask == null) {
            foundTask = findTaskFromQueue(workingQueue, dstTask);
        }
        if (foundTask == null) {
            foundTask = findTaskFromQueue(idleQueue, dstTask);
        }
        return foundTask;
    }

    private boolean containsInAllQueues(DownloadTask task) {
        if (waitingQueue.contains(task) || workingQueue.contains(task) || idleQueue.contains(task)) {
            return true;
        }
        return false;
    }

    DownloadTask addTask(DownloadTask task, DownloadListener listener) {
        if (!containsInAllQueues(task)) {
            task.setStatus(DownloadTask.Status.WAITING);
            waitingQueue.offer(task);

            final DownloadTask finalTask = task;
            daoExecutor.execute(() -> downloadTaskDBHandler.addTask(finalTask));
        } else {
            DownloadTask foundTask = findTaskFromQueue(idleQueue, task);
            if (foundTask != null) {
                // move to waiting queue
                moveTaskFromIdleToWaitingQueue(foundTask);
            } else {
                foundTask = findTaskFromQueue(waitingQueue, task);
                if (foundTask == null) {
                    foundTask = findTaskFromQueue(workingQueue, task);
                }
                if (foundTask != null) {
                    foundTask.increaseRetryCount();
                    task = foundTask;
                }
            }
        }
        if (task != null) {
            addListener(task.getUrl(), listener);
        }
        return task;
    }

    public DownloadTask addTask(String url, String destLocalPath, DownloadListener listener) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        if (url.startsWith("http")) {
            String localPath = destLocalPath;
            if (TextUtils.isEmpty(destLocalPath)) {
                localPath = FileUtils.getLocalFilePath(url, rootPath);
            }
            DownloadTask task = new DownloadTask();
            task.setStatus(DownloadTask.Status.WAITING);
            task.setUrl(url);
            task.setLocalPath(localPath);
            File localFile = new File(localPath);
            if (localFile.exists()) {
                if (listener != null) {
                    task.setFileTotalSize(localFile.length());
                    task.setDownloadedSize(localFile.length());
                    task.setProgress(100);
                    task.setStatus(DownloadTask.Status.DOWNLOADED);
                    listener.onDownloadProgress(task);
                    listener.onDownloadSuccess(task);
                }
                return task;
            } else {
                return addTask(task, listener);
            }
        }
        return null;
    }

    public void deleteTask(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        cancelWorkingTask(url);
        DownloadTask task = removeTaskFromQueue(url);
        if (task == null) {
            task = new DownloadTask();
            task.setUrl(url);
        }
        downloadTaskDBHandler.deleteTask(task);
        downloadDeleted(task);
    }

    public void deleteTask(DownloadTask task) {
        if (task == null) {
            return;
        }
        if (!TextUtils.isEmpty(task.getLocalPath())) {
            FileUtils.deleteFile(new File(task.getLocalPath()));
        }
        deleteTask(task.getUrl());
    }

    public void pauseTask(String url) {
        cancelWorkingTask(url);
        DownloadTask task = new DownloadTask();
        task.setUrl(url);
        DownloadTask foundTask = findTaskFromQueue(idleQueue, task);
        if (foundTask == null) {
            foundTask = findTaskFromQueue(waitingQueue, task);
            if (foundTask == null) {
                foundTask = findTaskFromQueue(workingQueue, task);
            }
            if (foundTask != null) {
                moveTaskToIdleQueue(foundTask);
            }
        }

        if (foundTask != null) {
            task = foundTask;
        }
        downloadPaused(task);
    }

    public void resumeTask(String url) {
        DownloadTask task = new DownloadTask();
        task.setUrl(url);
        DownloadTask foundTask = findTaskFromQueue(idleQueue, task);
        if (foundTask != null) {
            moveTaskFromIdleToWaitingQueue(foundTask);
            task = foundTask;
        } else {
            foundTask = findTaskFromQueue(waitingQueue, task);
            if (foundTask == null) {
                foundTask = findTaskFromQueue(workingQueue, task);
            }
            if (foundTask == null) {
                task = addTask(url, null, null);
            } else {
                task = foundTask;
            }
        }
        downloadResumed(task);
    }

    private void cancelWorkingTask(String url) {
        DownloadTask task = new DownloadTask();
        task.setUrl(url);
        for (DownloadWorker worker : workers) {
            if (task.equals(worker.getCurrentTask())) {
                worker.cancelCurrentTask();
            }
        }
    }

    private DownloadTask removeTaskFromQueue(String url) {
        DownloadTask task = new DownloadTask();
        task.setUrl(url);
        DownloadTask foundTask = findTaskInAllQueues(task);
        if (foundTask != null) {
            waitingQueue.remove(foundTask);
            workingQueue.remove(foundTask);
            idleQueue.remove(foundTask);
        }
        return foundTask;
    }

    public void loadTaskFromDb() {
        List<DownloadTask> tasks = downloadTaskDBHandler.getUnFinishedTasks();
        for (DownloadTask task : tasks) {
            moveTaskToIdleQueue(task);
        }
    }

    public void start() {
        loadTaskFromDb();
        taskExecutor = Executors.newFixedThreadPool(threads, new DownloadThreadFactory());
        for (int i = 0; i < threads; i++) {
            DownloadWorker worker = new DownloadWorker(this);
            workers.add(worker);
            taskExecutor.execute(worker);
        }
    }

    synchronized void downloadStart(DownloadTask task) {
        task.setStatus(DownloadTask.Status.DOWNLOADING);
        workingQueue.offer(task);
        downloadTaskDBHandler.updateTask(task);
        Set<DownloadListener> listeners = listenerMap.get(task.getUrl());
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                listener.onDownloadStart(task);
            }
        }
    }

    synchronized void onDownloadProgress(DownloadTask task, int progress) {
        task.setProgress(progress);
        Set<DownloadListener> listeners = listenerMap.get(task.getUrl());
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                listener.onDownloadProgress(task);
            }
        }
    }

    synchronized void downloadSuccess(DownloadTask task) {
        task.setStatus(DownloadTask.Status.DOWNLOADED);
        workingQueue.remove(task);
        downloadTaskDBHandler.updateTask(task);
        Set<DownloadListener> listeners = listenerMap.get(task.getUrl());
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                listener.onDownloadSuccess(task);
            }
        }
        removeListeners(task.getUrl());
    }

    synchronized void downloadFail(DownloadTask task) {
        task.setStatus(DownloadTask.Status.FAILED);
        workingQueue.remove(task);
        downloadTaskDBHandler.updateTask(task);
        Set<DownloadListener> listeners = listenerMap.get(task.getUrl());
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                listener.onDownloadFail(task);
            }
        }
        removeListeners(task.getUrl());
    }

    synchronized void downloadDeleted(DownloadTask task) {
        Set<DownloadListener> listeners = listenerMap.get(task.getUrl());
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                listener.onDownloadDeleted(task);
            }
        }
        removeListeners(task.getUrl());
    }

    synchronized void downloadPaused(DownloadTask task) {
        Set<DownloadListener> listeners = listenerMap.get(task.getUrl());
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                listener.onDownloadPaused(task);
            }
        }
    }

    synchronized void downloadResumed(DownloadTask task) {
        Set<DownloadListener> listeners = listenerMap.get(task.getUrl());
        if (listeners != null) {
            for (DownloadListener listener : listeners) {
                listener.onDownloadResumed(task);
            }
        }
    }

    public int getCurrentWorkerNum() {
        return workers.size();
    }

    Context getContext() {
        return context;
    }

    public static class Builder {
        private int threads = Constant.DEFAULT_THREAD_COUNT;
        private String rootPath;
        private Context context;
        private boolean needLog = true;
        private int logLevel = Log.INFO;
        public Builder(Context context) {
            this.context = context;
        }

        public Builder setThreads(int threads) {
            this.threads = threads;
            return this;
        }

        public Builder setRootPath(String rootPath) {
            this.rootPath = rootPath;
            return this;
        }

        public Builder setNeedLog(boolean needLog) {
            this.needLog = needLog;
            return this;
        }

        public Builder setLogLevel(int logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public DownloadManager build() {
            DownloadManager downloadManager = new DownloadManager();
            downloadManager.context = context.getApplicationContext();
            downloadManager.threads = threads;
            downloadManager.rootPath = rootPath;
            if (TextUtils.isEmpty(downloadManager.rootPath)) {
                downloadManager.rootPath = FileUtils.getCacheDir(context);
            }
            LogUtil.NEED_LOG = needLog;
            LogUtil.LOG_LEVEL = logLevel;
            DownloadDbHelper helper = new DownloadDbHelper(context.getApplicationContext());
            DownloadDbHelper.setInstance(helper);
            downloadManager.downloadTaskDBHandler = new TaskDBHandler();
            return downloadManager;
        }
    }
}
