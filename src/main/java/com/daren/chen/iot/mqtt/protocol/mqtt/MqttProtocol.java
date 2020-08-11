package com.daren.chen.iot.mqtt.protocol.mqtt;

import java.util.List;

import com.daren.chen.iot.mqtt.common.annocation.ProtocolType;
import com.daren.chen.iot.mqtt.protocol.BaseProtocolTransport;
import com.daren.chen.iot.mqtt.protocol.Protocol;
import com.google.common.collect.Lists;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;

/**
 *
 */
public class MqttProtocol implements Protocol {

    @Override
    public boolean support(ProtocolType protocolType) {
        return protocolType == ProtocolType.MQTT;
    }

    @Override
    public BaseProtocolTransport getTransport() {
        return new MqttTransport(this);
    }

    @Override
    public List<ChannelHandler> getHandlers() {
        return Lists.newArrayList(new MqttDecoder(5 * 1024 * 1024), MqttEncoder.INSTANCE);
    }
}
