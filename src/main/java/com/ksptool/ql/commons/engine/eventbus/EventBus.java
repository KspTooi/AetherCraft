package com.ksptool.ql.commons.engine.eventbus;

import com.ksptool.ql.commons.engine.eventlistener.*;
import com.ksptool.ql.commons.engine.threadfactories.EventThreadFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class EventBus implements ApplicationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventBus.class);
    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(16,new EventThreadFactory());

    //事件订阅者列表(异步)
    private final Map<String, List<AsyncEventConsumer>> asyncConsumers = new ConcurrentHashMap<>();

    //事件订阅者列表(同步)
    private final Map<String, List<SyncEventConsumer>> syncConsumers = new ConcurrentHashMap<>();

    /**
     * 添加一个函数式订阅
     * @param topic 主题
     * @param instance 函数执行实例
     * @param func 函数
     */
    public void subscribe(String topic,Object instance, Method func){
        ensureTopicExists(topic);

        //判断函数上是否有EventSubscribe注解
        var annoSubscribe = func.getAnnotation(EventSubscribe.class);

        if(annoSubscribe == null || func.getParameterCount() != 1){
            log.warn("[EVENT-BUS] 无法注册函数式事件处理器: {}->{}",instance.getClass().getCanonicalName(),func.getName());
            return;
        }

        //获取函数的第一个参数 判断是否实现了AsyncEvent || SyncEvent接口
        Parameter parameter = func.getParameters()[0];

        if (!AsyncEvent.class.isAssignableFrom(parameter.getType()) && !SyncEvent.class.isAssignableFrom(parameter.getType())) {
            log.warn("[EVENT-BUS] 无法注册函数式事件处理器: {}->{} 函数参数类型校验失败!",instance.getClass().getCanonicalName(),func.getName());
            return;
        }

        //注册为异步事件
        if(AsyncEvent.class.isAssignableFrom(parameter.getType())){
            var fcConsumer = new AsyncEventConsumer();
            fcConsumer.setType(1);//订阅者类型 0:事件接收器 1:函数式接收器
            fcConsumer.setTopic(topic);
            fcConsumer.setListener(null);
            fcConsumer.setFc(func);
            fcConsumer.setFcInstance(instance);
            asyncConsumers.get(topic).add(fcConsumer);
            log.info("Registered Async Success(FC) {}->{}",instance.getClass().getCanonicalName(),func.getName());
        }

        //注册为同步事件
        if(SyncEvent.class.isAssignableFrom(parameter.getType())){
            var fcConsumer = new SyncEventConsumer();
            fcConsumer.setOrder(annoSubscribe.order());
            fcConsumer.setType(1);//订阅者类型 0:事件接收器 1:函数式接收器
            fcConsumer.setTopic(topic);
            fcConsumer.setListener(null);
            fcConsumer.setFc(func);
            fcConsumer.setFcInstance(instance);
            syncConsumers.get(topic).add(fcConsumer);
            //重排序消费者列表
            sortSyncConsumers(topic);
            log.info("Registered Sync Success(FC) Order:{} {}->{}",annoSubscribe.order(),instance.getClass().getCanonicalName(),func.getName());
        }

    }

    /**
     * 添加一个接口订阅(异步)
     */
    public void subscribe(String topic, AsyncEventListener event) {
        ensureTopicExists(topic);
        var fcConsumer = new AsyncEventConsumer();
        fcConsumer.setType(0);//订阅者类型 0:事件接收器 1:函数式接收器
        fcConsumer.setTopic(topic);
        fcConsumer.setListener(event);
        fcConsumer.setFc(null);
        fcConsumer.setFcInstance(null);
        asyncConsumers.get(topic).add(fcConsumer);
        log.info("Registered Async Success {}",event.getClass().getName());
    }


    /**
     * 添加一个接口订阅(同步)
     */
    public void subscribe(String topic, SyncEventListener event) {
        subscribe(topic,0,event);
    }
    /**
     * 添加一个接口订阅(同步)
     */
    public void subscribe(String topic,int order, SyncEventListener event) {
        ensureTopicExists(topic);
        var fcConsumer = new SyncEventConsumer();
        fcConsumer.setOrder(order);
        fcConsumer.setType(0);//订阅者类型 0:事件接收器 1:函数式接收器
        fcConsumer.setTopic(topic);
        fcConsumer.setListener(event);
        fcConsumer.setFc(null);
        fcConsumer.setFcInstance(null);
        syncConsumers.get(topic).add(fcConsumer);
        //重排序消费者列表
        sortSyncConsumers(topic);
        log.info("Registered Sync Success Order:{} {}",order,event.getClass().getName());
    }


    /**
     * 使用缺省topic发布一个同步事件.
     * @param event 同步事件
     */
    public void send(SyncEvent event){
        send(event.getClass().getSimpleName(),event);
    }

    /**
     * 发布一个同步事件.该函数会因订阅者函数被阻塞.
     * @param topic 主题
     * @param event 同步事件
     */
    public void send(String topic, SyncEvent event){
        ensureTopicExists(topic);

        List<SyncEventConsumer> eventListeners = syncConsumers.get(topic);

        for (SyncEventConsumer eConsumer : eventListeners) {
            try{
                //订阅者类型 0:事件接收器 1:函数式接收器
                //是事件接收器
                if(eConsumer.getType() == 0){
                    eConsumer.getListener().onEventPushed(event);
                    return;
                }
                //是函数式接收器
                if(eConsumer.getType() == 1){
                    var fc = eConsumer.getFc();
                    fc.invoke(eConsumer.getFcInstance(),event);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * 发布一个异步事件.该调度函数本身不会被阻塞.
     * @param topic 主题
     * @param event 事件
     */
    public void send(String topic, AsyncEvent event) {
        ensureTopicExists(topic);
        List<AsyncEventConsumer> eventListeners = asyncConsumers.get(topic);
        for (AsyncEventConsumer eConsumer : eventListeners) {
            asyncExecutor.execute(() -> {
                try{
                    //订阅者类型 0:事件接收器 1:函数式接收器
                    //是事件接收器
                    if(eConsumer.getType() == 0){
                        eConsumer.getListener().onEventPushed(event);
                        return;
                    }
                    //是函数式接收器
                    if(eConsumer.getType() == 1){
                        var fc = eConsumer.getFc();
                        fc.invoke(eConsumer.getFcInstance(),event);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * 使用事件自身的缺省主题发布一个异步事件.该调度函数本身不会被阻塞.
     * @param event 事件
     */
    public void send(AsyncEvent event){
        //发布到默认topic
        send(event.getClass().getSimpleName(), event);
    }


    private void ensureTopicExists(String topic) {
        asyncConsumers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>());
        syncConsumers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>());
    }

    //重排序同步事件消费者
    private void sortSyncConsumers(String topic) {
        syncConsumers.get(topic).sort(Comparator.comparingInt(SyncEventConsumer::getOrder));
    }

    @Override
    public void publishEvent(@NotNull ApplicationEvent event) {
        throw new RuntimeException("no support event");
    }

    @Override
    public void publishEvent(@NotNull Object event) {

        if(event instanceof SyncEvent){
            send((SyncEvent)event);
            return;
        }

        if(event instanceof AsyncEvent){
            send((AsyncEvent)event);
            return;
        }

        throw new RuntimeException("no support event");
    }

    public static void initEventHandler(ApplicationContext ctx, EventBus eventBus){

        //初始化所有事件处理程序
        var eventHandlers = ctx.getBeansWithAnnotation(EventHandler.class);

        for(var entrySet : eventHandlers.entrySet()){

            Object eh = entrySet.getValue();

            var functions = eh.getClass().getMethods();

            for(var func : functions){

                //判断函数上是否有EventSubscribe注解
                var es = func.getAnnotation(EventSubscribe.class);

                if(es == null || func.getParameterCount() != 1){
                    continue;
                }

                //获取第一个参数 判断是否实现了AsyncEvent接口
                Parameter parameter = func.getParameters()[0];

                if(!AsyncEvent.class.isAssignableFrom(parameter.getType()) && !SyncEvent.class.isAssignableFrom(parameter.getType())){
                    continue;
                }

                //注册函数到事件总线
                eventBus.subscribe(parameter.getType().getSimpleName(),eh,func);
            }

        }

    }

}
