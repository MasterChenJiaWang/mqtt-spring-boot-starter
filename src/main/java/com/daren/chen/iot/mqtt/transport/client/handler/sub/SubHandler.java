package com.daren.chen.iot.mqtt.transport.client.handler.sub;

import com.daren.chen.iot.mqtt.api.RsocketConfiguration;
import com.daren.chen.iot.mqtt.api.TransportConnection;
import com.daren.chen.iot.mqtt.transport.DirectHandler;

import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;

/**
 * 订阅处理器
 */
public class SubHandler implements DirectHandler {

    @Override
    public void handler(MqttMessage message, TransportConnection connection, RsocketConfiguration config) {
        MqttFixedHeader header = message.fixedHeader();
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = (MqttMessageIdVariableHeader)message.variableHeader();
        switch (header.messageType()) {
            case SUBACK:
            case UNSUBACK:
                connection.cancelDisposable(mqttMessageIdVariableHeader.messageId());
                break;
            default:
                break;
        }
    }
}
