package com.ksptool.ql.commons.engine.eventlistener;

import java.lang.reflect.Method;

/**
 * 事件订阅者
 */
public class AsyncEventConsumer {

    //订阅主题
    private String topic;

    //订阅者类型 0:事件接收器 1:函数式接收器
    private int type;

    //事件接收器
    private AsyncEventListener listener;

    //函数式接收器
    private Method fc;

    //函数式接收器执行实例
    private Object fcInstance;


    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public AsyncEventListener getListener() {
        return listener;
    }

    public void setListener(AsyncEventListener listener) {
        this.listener = listener;
    }

    public Method getFc() {
        return fc;
    }

    public void setFc(Method fc) {
        this.fc = fc;
    }

    public Object getFcInstance() {
        return fcInstance;
    }

    public void setFcInstance(Object fcInstance) {
        this.fcInstance = fcInstance;
    }

    public void setType(int type) {
        this.type = type;
    }
}
