package com.daren.chen.iot.mqtt.transport.server;

import java.util.Objects;

import com.daren.chen.iot.mqtt.api.TransportConnection;
import com.daren.chen.iot.mqtt.api.server.RsocketServerSession;
import com.daren.chen.iot.mqtt.common.annocation.ProtocolType;
import com.daren.chen.iot.mqtt.config.RsocketServerConfig;
import com.daren.chen.iot.mqtt.protocol.ProtocolFactory;
import com.daren.chen.iot.mqtt.transport.server.connection.RsocketServerConnection;

import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.netty.DisposableServer;

public class TransportServerFactory {

    private final ProtocolFactory protocolFactory;

    private final UnicastProcessor<TransportConnection> unicastProcessor = UnicastProcessor.create();

    private RsocketServerConfig config;

    private DisposableServer wsServer;

    public TransportServerFactory() {
        protocolFactory = new ProtocolFactory();
    }

    /**
     *
     * @param config
     * @return
     */
    public Mono<RsocketServerSession> start(RsocketServerConfig config) {
        this.config = config;
        // 开启
        if (Objects.equals(config.getProtocol(), ProtocolType.MQTT.name())) {
            // TODO chendaren 这里 默认 开启 ws 端口 先关闭
            // WsTransport wsTransport = new WsTransport(new WsProtocol());
            // RsocketServerConfig wsConfig = copy(config);
            // wsServer = wsTransport.start(wsConfig, unicastProcessor).block();
        }
        return Mono.from(protocolFactory.getProtocol(ProtocolType.valueOf(config.getProtocol())).get().getTransport()
            .start(config, unicastProcessor)).map(this::wrapper).doOnError(config.getThrowableConsumer());
    }

    private RsocketServerConfig copy(RsocketServerConfig config) {
        RsocketServerConfig serverConfig = new RsocketServerConfig();
        serverConfig.setThrowableConsumer(config.getThrowableConsumer());
        serverConfig.setLog(config.isLog());
        serverConfig.setMessageHandler(config.getMessageHandler());
        serverConfig.setAuth(config.getAuth());
        serverConfig.setChannelManager(config.getChannelManager());
        serverConfig.setIp(config.getIp());
        serverConfig.setPort(8443);
        serverConfig.setSsl(config.isSsl());
        serverConfig.setProtocol(ProtocolType.WS_MQTT.name());
        serverConfig.setHeart(config.getHeart());
        serverConfig.setTopicManager(config.getTopicManager());
        serverConfig.setRevBufSize(config.getRevBufSize());
        serverConfig.setSendBufSize(config.getSendBufSize());
        serverConfig.setNoDelay(config.isNoDelay());
        serverConfig.setKeepAlive(config.isKeepAlive());
        return serverConfig;
    }

    private RsocketServerSession wrapper(DisposableServer server) {
        return new RsocketServerConnection(unicastProcessor, server, wsServer, config);
    }

}
