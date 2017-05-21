package com.cheney.filedownexample;

import android.app.Application;
import android.util.Log;

import com.cheney.filedownexample.constant.Constant;
import com.cheney.filedownexample.download.DownloadManager;

/**
 * Created by cheney on 2017/5/20.
 */

public class MainApp extends Application {
    private static MainApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        initDownload();
    }

    public static MainApp getInstance() {
        return instance;
    }

    /**
     * 初始化下载核心
     */
    private void initDownload() {
        DownloadManager downloadManager = new DownloadManager.Builder(this)
                .setNeedLog(true)
                .setLogLevel(Log.VERBOSE)
                .setThreads(Constant.MAX_DOWNLOAD_THREAD)
                .build();
        DownloadManager.setInstance(downloadManager);

        DownloadManager.getInstance().start();
    }
}
