package com.ksptool.ql.commons.engine.eventbus;

public class AbstractSyncEvent implements SyncEvent {

    private final String eventName;

    public AbstractSyncEvent(){
        this.eventName = this.getClass().getSimpleName();
    }

    @Override
    public String getEventName() {
        return eventName;
    }

}
