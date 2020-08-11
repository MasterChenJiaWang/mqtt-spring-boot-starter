package com.daren.chen.iot.mqtt.transport;

import com.daren.chen.iot.mqtt.api.RsocketConfiguration;
import com.daren.chen.iot.mqtt.api.TransportConnection;

import io.netty.handler.codec.mqtt.MqttMessage;

public interface DirectHandler {

    void handler(MqttMessage message, TransportConnection connection, RsocketConfiguration config);

}
