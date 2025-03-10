package com.ksptool.ql.commons.engine.threadfactories;


import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class EventThreadFactory implements ThreadFactory {

    private final AtomicLong threadNumber = new AtomicLong(0);

    @Override
    public Thread newThread(@NotNull Runnable r) {

        if(threadNumber.get() > 32 * 16384){
            threadNumber.set(0);
        }

        var t = new Thread(r);
        t.setName("AsyncEventThread-"+threadNumber.getAndIncrement());
        return t;
    }

}
