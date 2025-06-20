package com.ksptool.ql.commons.engine.eventlistener;


import com.ksptool.ql.commons.engine.eventbus.AsyncEvent;
import org.springframework.stereotype.Component;

@Component
public interface AsyncEventListener {
    void onEventPushed(AsyncEvent event);
}
