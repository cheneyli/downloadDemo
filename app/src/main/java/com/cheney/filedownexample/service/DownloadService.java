package com.cheney.filedownexample.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import com.cheney.filedownexample.constant.Constant;
import com.cheney.filedownexample.constant.ExtraInfo;
import com.cheney.filedownexample.download.DownloadManager;
import com.cheney.filedownexample.download.DownloadTask;
import com.cheney.filedownexample.events.DownloadStatusEvent;
import com.cheney.filedownexample.handler.EventSender;
import com.cheney.filedownexample.listener.DownloadListener;
import com.cheney.filedownexample.utils.FileUtils;
import com.cheney.filedownexample.utils.LogUtil;

/**
 * Created by cheney on 2017/5/20.
 */
public class DownloadService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Constant.ACTION_START.equals(intent.getAction())) {
            String fileUrl = intent.getStringExtra(ExtraInfo.FILE_URL);
            boolean isPending = intent.getBooleanExtra(ExtraInfo.FILE_DOWNLOAD_PENDING, false);
            downloadFile(fileUrl, isPending);
        } else if (Constant.ACTION_STOP.equals(intent.getAction())) {
            String fileUrl = intent.getStringExtra(ExtraInfo.FILE_URL);
            LogUtil.logI("tonStartCommand fileUrl %s", fileUrl);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void downloadFile(String fileUrl, boolean isPending) {
        Uri uri = Uri.parse(fileUrl);
        LogUtil.logI("tonStartCommand fileUrl %s", fileUrl);
        DownloadManager.getInstance().addTask(fileUrl, FileUtils.getFileDir() + uri.getLastPathSegment(), new DownloadListener() {
            @Override
            public void onDownloadStart(DownloadTask task) {
                LogUtil.logI("onDownloadStart fileUrl %s", fileUrl);
                EventSender.getInstance().sendDownloadStatusEvent(new DownloadStatusEvent(task));
            }

            @Override
            public void onDownloadProgress(DownloadTask task) {
                LogUtil.logI("onDownloadProgress fileUrl %s", fileUrl);
                EventSender.getInstance().sendDownloadStatusEvent(new DownloadStatusEvent(task));
            }

            @Override
            public void onDownloadSuccess(DownloadTask task) {
                LogUtil.logI("onDownloadSuccess fileUrl %s", fileUrl);
                EventSender.getInstance().sendDownloadStatusEvent(new DownloadStatusEvent(task));
            }

            @Override
            public void onDownloadFail(DownloadTask task) {
                LogUtil.logI("onDownloadFail fileUrl %s", fileUrl);
                EventSender.getInstance().sendDownloadStatusEvent(new DownloadStatusEvent(task));
            }

            @Override
            public void onDownloadDeleted(DownloadTask task) {
                LogUtil.logI("onDownloadDeleted fileUrl %s", fileUrl);
                EventSender.getInstance().sendDownloadStatusEvent(new DownloadStatusEvent(task));
            }

            @Override
            public void onDownloadPaused(DownloadTask task) {
                LogUtil.logI("onDownloadPaused fileUrl %s", fileUrl);
                EventSender.getInstance().sendDownloadStatusEvent(new DownloadStatusEvent(task));
            }

            @Override
            public void onDownloadResumed(DownloadTask task) {
                LogUtil.logI("onDownloadResumed fileUrl %s", fileUrl);
                EventSender.getInstance().sendDownloadStatusEvent(new DownloadStatusEvent(task));
            }
        });
    }
}
