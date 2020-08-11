package com.daren.chen.iot.mqtt.transport.client;

import com.daren.chen.iot.mqtt.api.TransportConnection;
import com.daren.chen.iot.mqtt.api.client.RsocketClientSession;
import com.daren.chen.iot.mqtt.common.annocation.ProtocolType;
import com.daren.chen.iot.mqtt.config.RsocketClientConfig;
import com.daren.chen.iot.mqtt.protocol.ProtocolFactory;
import com.daren.chen.iot.mqtt.protocol.mqtt.MqttProtocol;
import com.daren.chen.iot.mqtt.transport.client.connection.RsocketClientConnection;

import reactor.core.publisher.Mono;

/**
 *
 */
public class TransportClientFactory {

    /**
     *
     */
    private ProtocolFactory protocolFactory;

    /**
     *
     */
    private RsocketClientConfig clientConfig;

    public TransportClientFactory() {
        protocolFactory = new ProtocolFactory();
    }

    public Mono<RsocketClientSession> connect(RsocketClientConfig config) {
        this.clientConfig = config;
        // 默认 mqtt
        return Mono.from(protocolFactory.getProtocol(ProtocolType.valueOf(config.getProtocol()))
            .orElse(new MqttProtocol()).getTransport().connect(config)).map(this::wrapper)
            .doOnError(config.getThrowableConsumer());
    }

    private RsocketClientSession wrapper(TransportConnection connection) {
        return new RsocketClientConnection(connection, clientConfig);
    }

}
