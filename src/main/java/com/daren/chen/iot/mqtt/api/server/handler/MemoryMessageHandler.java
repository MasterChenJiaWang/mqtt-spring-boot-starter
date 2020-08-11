package com.daren.chen.iot.mqtt.api.server.handler;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.daren.chen.iot.mqtt.api.RsocketMessageHandler;
import com.daren.chen.iot.mqtt.common.connection.RetainMessage;

/**
 * 内存消息处理器 默认是把消息放入内存,也可自定义实现RsocketMessageHandler 处理消息
 */
public class MemoryMessageHandler implements RsocketMessageHandler {

    /**
     *
     */
    private final Map<String, RetainMessage> messages = new ConcurrentHashMap<>();

    /**
     * 保存消息
     *
     * @param dup
     * @param retain
     * @param qos
     * @param topicName
     * @param copyByteBuf
     */
    @Override
    public void saveRetain(boolean dup, boolean retain, int qos, String topicName, byte[] copyByteBuf) {
        messages.put(topicName, new RetainMessage(dup, retain, qos, topicName, copyByteBuf));
    }

    /**
     * 根据topicname 获取消息
     *
     * @param topicName
     * @return
     */
    @Override
    public Optional<RetainMessage> getRetain(String topicName) {
        return Optional.ofNullable(messages.get(topicName));
    }
}
