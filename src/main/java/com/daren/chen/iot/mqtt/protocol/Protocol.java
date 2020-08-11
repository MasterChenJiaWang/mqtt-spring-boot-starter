package com.daren.chen.iot.mqtt.protocol;

import java.util.List;

import com.daren.chen.iot.mqtt.common.annocation.ProtocolType;

import io.netty.channel.ChannelHandler;

public interface Protocol {

    boolean support(ProtocolType protocolType);

    BaseProtocolTransport getTransport();

    List<ChannelHandler> getHandlers();

}
