package com.daren.chen.iot.mqtt.transport.server.handler;

import com.daren.chen.iot.mqtt.api.TransportConnection;
import com.daren.chen.iot.mqtt.config.RsocketServerConfig;
import com.daren.chen.iot.mqtt.transport.DirectHandler;

import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务端消息路由
 */
@Getter
@Slf4j
public class ServerMessageRouter {

    /**
     *
     */
    private final RsocketServerConfig config;

    /**
     * 处理适配器
     */
    private final DirectHandlerAdaptor directHandlerAdaptor;

    public ServerMessageRouter(RsocketServerConfig config) {
        this.config = config;
        this.directHandlerAdaptor = DirectHandlerFactory::new;
    }

    /**
     *
     * @param message
     * @param connection
     */
    public void handler(MqttMessage message, TransportConnection connection) {
        if (message.decoderResult().isSuccess()) {
            log.info("server accept message channel {} info{}", connection.getConnection(), message);
            DirectHandler handler = directHandlerAdaptor.handler(message.fixedHeader().messageType()).loadHandler();
            handler.handler(message, connection, config);
        } else {
            log.error("accept message  error{}", message.decoderResult().toString());
        }
    }

}
