package com.cheney.filedownexample.handler.interfaces;

import com.cheney.filedownexample.events.DownloadStatusEvent;

/**
 * Created by cheney on 2017/5/20.
 */

public interface IEventSender {
    void register(Object obj);

    void unregister(Object obj);

    void sendDownloadStatusEvent(DownloadStatusEvent downloadStatusEvent);
}
