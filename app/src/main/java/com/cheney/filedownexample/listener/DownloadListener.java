package com.cheney.filedownexample.listener;

import com.cheney.filedownexample.download.DownloadTask;

/**
 * Created by cheney on 2017/5/20.
 */

public interface DownloadListener {
    void onDownloadStart(DownloadTask task);

    void onDownloadProgress(DownloadTask task);

    void onDownloadSuccess(DownloadTask task);

    void onDownloadFail(DownloadTask task);

    void onDownloadDeleted(DownloadTask task);

    void onDownloadPaused(DownloadTask task);

    void onDownloadResumed(DownloadTask task);
}
