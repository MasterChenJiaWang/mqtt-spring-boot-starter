package com.daren.chen.iot.mqtt.transport.server.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.daren.chen.iot.mqtt.common.exception.NotSuppportHandlerException;
import com.daren.chen.iot.mqtt.transport.DirectHandler;
import com.daren.chen.iot.mqtt.transport.server.handler.connect.ConnectHandler;
import com.daren.chen.iot.mqtt.transport.server.handler.heart.HeartHandler;
import com.daren.chen.iot.mqtt.transport.server.handler.pub.PubHandler;
import com.daren.chen.iot.mqtt.transport.server.handler.sub.SubHandler;

import io.netty.handler.codec.mqtt.MqttMessageType;

/**
 *
 */
public class DirectHandlerFactory {

    private final MqttMessageType messageType;

    private final Map<MqttMessageType, DirectHandler> messageTypeCollection = new ConcurrentHashMap<>();

    public DirectHandlerFactory(MqttMessageType messageType) {
        this.messageType = messageType;
    }

    /**
     *
     * @return
     */
    public DirectHandler loadHandler() {
        return messageTypeCollection.computeIfAbsent(messageType, type -> {
            switch (type) {
                // 下面5种相同
                case PUBACK:
                case PUBREC:
                case PUBREL:
                case PUBLISH:
                case PUBCOMP:
                    return new PubHandler();
                // CONNECT DISCONNECT 相同
                case CONNECT:
                case DISCONNECT:
                    return new ConnectHandler();
                case PINGREQ:
                    return new HeartHandler();
                case SUBSCRIBE:
                case UNSUBSCRIBE:
                    return new SubHandler();
                default:
                    break;
            }
            throw new NotSuppportHandlerException(messageType + " not support ");
        });
    }

}
