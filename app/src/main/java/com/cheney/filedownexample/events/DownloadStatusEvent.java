package com.cheney.filedownexample.events;

import com.cheney.filedownexample.download.DownloadTask;

/**
 * Created by cheney on 2017/5/20.
 */

public class DownloadStatusEvent {
    public DownloadTask downloadTask;

    public DownloadStatusEvent(DownloadTask downloadTask) {
        this.downloadTask = downloadTask;
    }
}
