package com.daren.chen.iot.mqtt.protocol.ws;

import java.util.List;

import com.daren.chen.iot.mqtt.common.annocation.ProtocolType;
import com.daren.chen.iot.mqtt.protocol.BaseProtocolTransport;
import com.daren.chen.iot.mqtt.protocol.Protocol;
import com.google.common.collect.Lists;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;

public class WsProtocol implements Protocol {

    @Override
    public boolean support(ProtocolType protocolType) {
        return protocolType == ProtocolType.WS_MQTT;
    }

    @Override
    public BaseProtocolTransport getTransport() {
        return new WsTransport(this);
    }

    @Override
    public List<ChannelHandler> getHandlers() {
        return Lists.newArrayList(new HttpServerCodec(), new HttpObjectAggregator(65536),
            new WebSocketServerProtocolHandler("/", "mqtt, mqttv3.1, mqttv3.1.1"), new WebSocketFrameToByteBufDecoder(),
            new ByteBufToWebSocketFrameEncoder(), new MqttDecoder(5 * 1024 * 1024), MqttEncoder.INSTANCE);
    }
}
