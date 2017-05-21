package com.cheney.filedownexample.download;

import com.cheney.filedownexample.utils.FileUtils;
import com.cheney.filedownexample.utils.LogUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by cheney on 2017/5/20.
 */

public class DownloadWorker implements Runnable {
    private static final int TIMEOUT = 10 * 1000;

    static final AtomicLong seq = new AtomicLong(0);
    private final long seqNum;

    private DownloadManager downloadManager;
    private DownloadTask currentTask;
    private boolean taskCancelled = false;


    public DownloadWorker(DownloadManager downloadManager) {
        seqNum = seq.getAndIncrement();
        this.downloadManager = downloadManager;
    }

    public synchronized DownloadTask getCurrentTask() {
        return currentTask;
    }

    private synchronized void setCurrentTask(DownloadTask currentTask) {
        this.currentTask = currentTask;
    }

    private synchronized void setTaskCancelled(boolean taskCancelled) {
        this.taskCancelled = taskCancelled;
    }

    private synchronized boolean isTaskCancelled() {
        return taskCancelled;
    }

    @Override
    public void run() {
        LogUtil.logI("[%s] running...", this);
        while (true) {
            try {
                DownloadTask task = downloadManager.takeFromWaitingQueue();
                setCurrentTask(task);
                downloadManager.downloadStart(task);
                download(task);
            } catch (InterruptedException e) {
                LogUtil.logW(e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                LogUtil.logW(e.getMessage());
                e.printStackTrace();
                DownloadTask task = getCurrentTask();
                if (task != null) {
                    if (task.getRetryCount() > 0) {
                        LogUtil.logI("[%s] retry download url: %s", this, task.getUrl());
                        task.decreaseRetryCount();
                        downloadManager.addRetryTask(task);
                    } else {
                        LogUtil.logI("[%s] download failed url: %s", this, task.getUrl());
                        downloadManager.downloadFail(task);
                    }
                    setCurrentTask(null);
                }
            }
        }
    }

    private void download(DownloadTask task) throws IOException {
        long startTime = System.currentTimeMillis();
        LogUtil.logI("[%s] Download task - url: %s", this, task.getUrl());
        setTaskCancelled(false);
        String tmpFilePath = task.getLocalPath() + ".tmp";
        File tmpFile = new File(tmpFilePath);
        long downloadedSize = 0;
        if (tmpFile.exists()) {
            // need continue previous download
            downloadedSize = tmpFile.length();
        } else {
            File parentDir = tmpFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            LogUtil.logI("[%s] Download task - tmpFile: %s", this, tmpFile.getAbsolutePath());
            tmpFile.createNewFile();
        }

        task.setStatus(DownloadTask.Status.DOWNLOADING);
        HttpURLConnection connection = null;
        try {
            URL url = new URL(task.getUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);

            LogUtil.logI("[%s] Download range from %d -", this, downloadedSize);
            connection.setRequestProperty("Range", "bytes=" + downloadedSize + "-");
            connection.setRequestProperty("Accept-Encoding", "identity");
            connection.connect();

            long leftSize = connection.getContentLength();
            long totalSize = leftSize + downloadedSize;
            LogUtil.logI("[%s] File total size: %d, need download size: %d", this, totalSize, leftSize);
            if (task.getFileTotalSize() > 0 && task.getFileTotalSize() != totalSize) {
                // file updated, need download from start
                task.setFileTotalSize(0);
                task.setDownloadedSize(0);
                FileUtils.deleteFile(tmpFile);
                download(task);
                return;
            } else {
                task.setFileTotalSize(totalSize);
                task.setDownloadedSize(downloadedSize);
            }

            InputStream is = null;
            RandomAccessFile fileOutput = null;
            try {
                int bufferSize = 250 * 1024;
                ;
                LogUtil.logI("[%s] current buffer size: %d", this, bufferSize);
                is = new BufferedInputStream(connection.getInputStream(), bufferSize);
                fileOutput = new RandomAccessFile(tmpFile, "rwd");
                fileOutput.seek(downloadedSize);

                byte[] buffer = new byte[bufferSize];
                int readLength;
                while (!isTaskCancelled()
                        && (readLength = is.read(buffer)) > 0) {
                    fileOutput.write(buffer, 0, readLength);
                    downloadedSize += readLength;
                    int progress = (int) (downloadedSize * 100 / totalSize);
                    progress = Math.max(0, progress);
                    progress = Math.min(100, progress);
                    task.setDownloadedSize(downloadedSize);
                    LogUtil.logD("[%s] downloaded size: %d progress: %d", this, downloadedSize, progress);
                    downloadManager.onDownloadProgress(task, progress);
                }
                task.setDownloadedSize(downloadedSize);
                LogUtil.logI("[%s] downloaded size: %d", this, downloadedSize);
                if (isTaskCancelled()) {
                    LogUtil.logI("[%s] task cancelled", this);
                    setCurrentTask(null);
                    return;
                }
                boolean ret = FileUtils.renameFile(tmpFile, task.getLocalPath());
                if (ret) {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
                    int speed;
                    if (seconds != 0) {
                        speed = (int) (leftSize / (1024 * seconds));
                    } else if (duration != 0) {
                        speed = (int) (leftSize * 1000 / (1024 * duration));
                    } else {
                        speed = Integer.MAX_VALUE;
                    }
                    LogUtil.logI("[%s] download success url: %s cost time: %d(s) speed: %d(kB/s)", this, task.getUrl(), seconds, speed);
                    downloadManager.onDownloadProgress(task, 100);
                    downloadManager.downloadSuccess(task);
                } else {
                    LogUtil.logW("[%s] rename file failed after download - set task as failed", this);
                    downloadManager.downloadFail(task);
                }
                setCurrentTask(null);
            } finally {
                LogUtil.logI("[%s] close input stream and file output", this);
                if (is != null) {
                    is.close();
                    is = null;
                }
                if (fileOutput != null) {
                    fileOutput.close();
                    fileOutput = null;
                }
            }
        } finally {
            LogUtil.logI("[%s] close http connection", this);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void cancelCurrentTask() {
        setTaskCancelled(true);
    }

    @Override
    public String toString() {
        return "DownloadWorker-" + seqNum;
    }
}


