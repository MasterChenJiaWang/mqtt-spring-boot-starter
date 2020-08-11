package com.daren.chen.iot.mqtt.transport.client.handler;

import io.netty.handler.codec.mqtt.MqttMessageType;

/**
 *
 */
public interface DirectHandlerAdaptor {

    /**
     *
     * @param messageType
     * @return
     */
    DirectHandlerFactory handler(MqttMessageType messageType);

}
