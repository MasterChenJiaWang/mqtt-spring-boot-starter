package com.daren.chen.iot.mqtt.container.topic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandlerFactory {
    private HandlerFactory() {

    }

    private static class InstanceHolder {
        private static final HandlerFactory HANDLER_FACTORY = new HandlerFactory();
    }

    public static HandlerFactory getInstance() {
        return InstanceHolder.HANDLER_FACTORY;
    }

    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    public void putHandler(String topic, Object handler) {
        singletonObjects.put(topic, handler);
    }

    public Object getHandler(String topic) {
        return singletonObjects.get(topic);
    }
}
