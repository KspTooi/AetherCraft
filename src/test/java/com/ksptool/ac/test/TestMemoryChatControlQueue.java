package com.ksptool.ac.test;

import com.ksptool.ql.commons.exception.BizException;
import com.ksptool.ql.commons.utils.mccq.ChatFragment;
import com.ksptool.ql.commons.utils.mccq.MemoryChatControlQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeoutException;

public class TestMemoryChatControlQueue {

    private MemoryChatControlQueue mccq;

    @BeforeEach
    public void setUp(){
        mccq = new MemoryChatControlQueue();
    }


    @Test
    public void test() throws InterruptedException, BizException, TimeoutException {

        mccq.setFragmentTtl(16);

        var threadId = 100;

        for(var i = 0; i < 100; i++) {
            ChatFragment cf = new ChatFragment();
            cf.setType(1);
            cf.setPlayerId(1);
            cf.setThreadId(threadId);
            cf.setContent("hhh");
            mccq.receive(cf);
        }

        while (true){
            Thread.sleep(1000);
            ChatFragment next = mccq.next(threadId);
            System.out.println("next:" + next);
        }


    }





}
