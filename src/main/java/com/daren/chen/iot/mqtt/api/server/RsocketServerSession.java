package com.daren.chen.iot.mqtt.api.server;

import java.util.List;

import com.daren.chen.iot.mqtt.api.TransportConnection;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

/**
 *
 */
public interface RsocketServerSession extends Disposable {

    /**
     *
     * @return
     */
    Mono<List<TransportConnection>> getConnections();

    /**
     *
     * @param clientId
     * @return
     */
    Mono<Void> closeConnect(String clientId);

}
