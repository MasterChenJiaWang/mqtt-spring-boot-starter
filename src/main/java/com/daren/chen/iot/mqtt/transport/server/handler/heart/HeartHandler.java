package com.daren.chen.iot.mqtt.transport.server.handler.heart;

import com.daren.chen.iot.mqtt.api.RsocketConfiguration;
import com.daren.chen.iot.mqtt.api.TransportConnection;

import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * 心跳处理器
 */
@Slf4j
public class HeartHandler implements com.daren.chen.iot.mqtt.transport.DirectHandler {

    /**
     *
     * @param message
     * @param connection
     * @param config
     */
    @Override
    public void handler(MqttMessage message, TransportConnection connection, RsocketConfiguration config) {
        switch (message.fixedHeader().messageType()) {
            case PINGREQ:
                connection.sendPingRes().subscribe();
                break;
            case PINGRESP:
                break;
            default:
                break;
        }

    }
}
