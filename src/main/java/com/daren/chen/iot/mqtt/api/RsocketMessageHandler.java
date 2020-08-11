package com.daren.chen.iot.mqtt.api;

import java.util.Optional;

import com.daren.chen.iot.mqtt.common.connection.RetainMessage;

/**
 * manage message
 */
public interface RsocketMessageHandler {

    /**
     *
     * @param dup
     * @param retain
     * @param qos
     * @param topicName
     * @param copyByteBuf
     */
    void saveRetain(boolean dup, boolean retain, int qos, String topicName, byte[] copyByteBuf);

    /**
     *
     * @param topicName
     * @return
     */
    Optional<RetainMessage> getRetain(String topicName);
}
