package com.daren.chen.iot.mqtt.container.topic;

import javax.annotation.Resource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

public class ProxyFactoryBean<T> implements FactoryBean<T> {

    private Class<T> rpcInterface;

    @Resource
    private ApplicationContext applicationContext;

    private final HandlerFactory handlerFactory = HandlerFactory.getInstance();

    public ProxyFactoryBean() {

    }

    public ProxyFactoryBean(Class<T> rpcInterface) {
        this.rpcInterface = rpcInterface;
        try {
            handlerFactory.putHandler(getName(), rpcInterface.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * 用描述文件,生成真正对象的时候,会调用这个方法 调用的时候生成代理对象
     *
     * @return
     * @throws Exception
     */
    @Override
    public T getObject() throws Exception {
        return (T)handlerFactory.getHandler(getName());
    }

    private String getName() {
        TopicHandler topicHandler = rpcInterface.getAnnotation(TopicHandler.class);
        String serviceName = topicHandler.topic();
        if (StringUtils.isEmpty(serviceName)) {
            serviceName = rpcInterface.getName();
        }
        return serviceName;
    }

    /**
     * 假装我的类型还是 原来的接口类型,不是代理对象 这样 自动注入的时候,类型才能匹配上
     *
     * @return
     */
    @Override
    public Class<?> getObjectType() {
        return rpcInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public Class<T> getRpcInterface() {
        return rpcInterface;
    }

    public void setRpcInterface(Class<T> rpcInterface) {
        this.rpcInterface = rpcInterface;
    }

}
