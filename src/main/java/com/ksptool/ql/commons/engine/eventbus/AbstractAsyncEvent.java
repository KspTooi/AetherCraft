package com.ksptool.ql.commons.engine.eventbus;

public class AbstractAsyncEvent implements AsyncEvent {

    private final String eventName;

    public AbstractAsyncEvent(){
        this.eventName = this.getClass().getSimpleName();
    }

    @Override
    public String getEventName() {
        return eventName;
    }

}
