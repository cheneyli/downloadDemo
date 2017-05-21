package com.cheney.filedownexample.handler;

import com.cheney.filedownexample.events.DownloadStatusEvent;
import com.cheney.filedownexample.handler.interfaces.IEventSender;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by cheney on 2017/5/20.
 */

public class EventSender implements IEventSender {
    private static EventSender instance;

    private EventSender() {}

    public static IEventSender getInstance() {
        if (instance == null) {
            synchronized (EventSender.class) {
                if (instance == null) {
                    instance = new EventSender();
                }
            }
        }

        return instance;
    }


    @Override
    public void register(Object obj) {
        if(EventBus.getDefault().isRegistered(obj)){
            return;
        }
        try {
            EventBus.getDefault().register(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unregister(Object obj) {
        if(EventBus.getDefault().isRegistered(obj)){
            EventBus.getDefault().unregister(obj);
        }
    }

    @Override
    public void sendDownloadStatusEvent(DownloadStatusEvent downloadStatusEvent) {
        EventBus.getDefault().post(downloadStatusEvent);
    }
}
