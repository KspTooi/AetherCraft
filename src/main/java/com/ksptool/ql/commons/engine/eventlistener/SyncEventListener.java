package com.ksptool.ql.commons.engine.eventlistener;


import com.ksptool.ql.commons.engine.eventbus.SyncEvent;

public interface SyncEventListener {
    public void onEventPushed(SyncEvent event);
}
