package com.daren.chen.iot.mqtt.protocol;

import com.daren.chen.iot.mqtt.api.RsocketConfiguration;
import com.daren.chen.iot.mqtt.api.TransportConnection;

import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.netty.DisposableServer;

public abstract class BaseProtocolTransport {

    protected Protocol protocol;

    public BaseProtocolTransport(Protocol protocol) {
        this.protocol = protocol;
    }

    public abstract Mono<? extends DisposableServer> start(RsocketConfiguration config,
        UnicastProcessor<TransportConnection> connections);

    public abstract Mono<TransportConnection> connect(RsocketConfiguration config);

}
