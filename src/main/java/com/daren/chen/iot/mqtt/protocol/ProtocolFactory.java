package com.daren.chen.iot.mqtt.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.daren.chen.iot.mqtt.common.annocation.ProtocolType;
import com.daren.chen.iot.mqtt.protocol.mqtt.MqttProtocol;
import com.daren.chen.iot.mqtt.protocol.ws.WsProtocol;

/**
 * 协议工厂
 */
public class ProtocolFactory {

    private final List<Protocol> protocols = new ArrayList<>();

    /**
     * 支持 mqtt ws
     */
    public ProtocolFactory() {
        protocols.add(new MqttProtocol());
        protocols.add(new WsProtocol());
    }

    /**
     * 注册协议
     *
     * @param protocol
     */
    public void registryProtocl(Protocol protocol) {
        protocols.add(protocol);
    }

    /**
     * 获取协议
     *
     * @param protocolType
     * @return
     */
    public Optional<Protocol> getProtocol(ProtocolType protocolType) {
        return protocols.stream().filter(protocol -> protocol.support(protocolType)).findAny();
    }

}
