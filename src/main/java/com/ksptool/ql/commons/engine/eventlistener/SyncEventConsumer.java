package com.ksptool.ql.commons.engine.eventlistener;

import java.lang.reflect.Method;

/**
 * 事件订阅者
 */
public class SyncEventConsumer {

    //优先级
    private int order;

    //订阅主题
    private String topic;

    //订阅者类型 0:事件接收器 1:函数式接收器
    private int type;

    //事件接收器
    private SyncEventListener listener;

    //函数式接收器
    private Method fc;

    //函数式接收器执行实例
    private Object fcInstance;


    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public SyncEventListener getListener() {
        return listener;
    }

    public void setListener(SyncEventListener listener) {
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
}
