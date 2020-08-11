package com.daren.chen.iot.mqtt.transport.client.handler;

import com.daren.chen.iot.mqtt.api.TransportConnection;
import com.daren.chen.iot.mqtt.config.RsocketClientConfig;
import com.daren.chen.iot.mqtt.transport.DirectHandler;

import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class ClientMessageRouter {

    private final RsocketClientConfig config;

    private final DirectHandlerAdaptor directHandlerAdaptor;

    public ClientMessageRouter(RsocketClientConfig config) {
        this.config = config;
        this.directHandlerAdaptor = DirectHandlerFactory::new;
    }

    /**
     *
     * @param message
     * @param connection
     */
    public void handler(MqttMessage message, TransportConnection connection) {
        log.info("client accept message  info{}", message);
        try {
            if (message.decoderResult().isSuccess()) {
                DirectHandler handler = directHandlerAdaptor.handler(message.fixedHeader().messageType()).loadHandler();
                handler.handler(message, connection, config);
            } else {
                log.error("accept message  error{}", message.decoderResult().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
