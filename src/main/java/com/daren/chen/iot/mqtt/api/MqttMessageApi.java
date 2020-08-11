package com.daren.chen.iot.mqtt.api;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectPayload;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttSubscribePayload;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribePayload;
import io.netty.handler.codec.mqtt.MqttVersion;

/**
 * mqtt 消息api
 */
public class MqttMessageApi {

    /**
     * 构建发布消息
     *
     * @param isDup
     * @param qoS
     * @param isRetain
     * @param messageId
     * @param topic
     * @param message
     * @return
     */
    public static MqttPublishMessage buildPub(boolean isDup, MqttQoS qoS, boolean isRetain, int messageId, String topic,
        ByteBuf message) {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, isDup, qoS, isRetain, 0);
        MqttPublishVariableHeader mqttPublishVariableHeader = new MqttPublishVariableHeader(topic, messageId);
        return new MqttPublishMessage(mqttFixedHeader, mqttPublishVariableHeader, message);
    }

    /**
     *
     * @param isDup
     * @param qoS
     * @param isRetain
     * @param messageId
     * @return
     */
    public static MqttPubAckMessage buildPuback(boolean isDup, MqttQoS qoS, boolean isRetain, int messageId) {
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, isDup, qoS, isRetain, 2);
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        return new MqttPubAckMessage(mqttFixedHeader, mqttMessageIdVariableHeader);
    }

    /**
     *
     * @param messageId
     * @return
     */
    public static MqttPubAckMessage buildPubRec(int messageId) {
        MqttFixedHeader mqttFixedHeader =
            new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_LEAST_ONCE, false, 0x02);
        MqttMessageIdVariableHeader from = MqttMessageIdVariableHeader.from(messageId);
        return new MqttPubAckMessage(mqttFixedHeader, from);
    }

    /**
     *
     * @param messageId
     * @return
     */
    public static MqttPubAckMessage buildPubRel(int messageId) {
        MqttFixedHeader mqttFixedHeader =
            new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_LEAST_ONCE, false, 0x02);
        MqttMessageIdVariableHeader from = MqttMessageIdVariableHeader.from(messageId);
        return new MqttPubAckMessage(mqttFixedHeader, from);
    }

    /**
     *
     * @param messageId
     * @return
     */
    public static MqttPubAckMessage buildPubComp(int messageId) {
        MqttFixedHeader mqttFixedHeader =
            new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.AT_MOST_ONCE, false, 0x02);
        MqttMessageIdVariableHeader from = MqttMessageIdVariableHeader.from(messageId);
        return new MqttPubAckMessage(mqttFixedHeader, from);
    }

    /**
     *
     * @param messageId
     * @param qos
     * @return
     */
    public static MqttSubAckMessage buildSubAck(int messageId, List<Integer> qos) {
        MqttFixedHeader mqttFixedHeader =
            new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(messageId);
        MqttSubAckPayload payload = new MqttSubAckPayload(qos);
        return new MqttSubAckMessage(mqttFixedHeader, variableHeader, payload);
    }

    /**
     *
     * @param messageId
     * @return
     */
    public static MqttUnsubAckMessage buildUnsubAck(int messageId) {
        MqttFixedHeader mqttFixedHeader =
            new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0x02);
        MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(messageId);
        return new MqttUnsubAckMessage(mqttFixedHeader, variableHeader);
    }

    /**
     *
     * @param connectReturnCode
     * @return
     */
    public static MqttConnAckMessage buildConnectAck(MqttConnectReturnCode connectReturnCode) {
        MqttConnAckVariableHeader mqttConnAckVariableHeader = new MqttConnAckVariableHeader(connectReturnCode, true);
        MqttFixedHeader mqttFixedHeader =
            new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0x02);
        return new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
    }

    /**
     *
     * @param messageId
     * @param topicSubscriptions
     * @return
     */
    public static MqttSubscribeMessage buildSub(int messageId, List<MqttTopicSubscription> topicSubscriptions) {
        MqttSubscribePayload mqttSubscribePayload = new MqttSubscribePayload(topicSubscriptions);
        MqttFixedHeader mqttFixedHeader =
            new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, 0);
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(messageId);
        return new MqttSubscribeMessage(mqttFixedHeader, mqttMessageIdVariableHeader, mqttSubscribePayload);
    }

    /**
     *
     * @param messageId
     * @param topics
     * @return
     */
    public static MqttUnsubscribeMessage buildUnSub(int messageId, List<String> topics) {
        MqttFixedHeader mqttFixedHeader =
            new MqttFixedHeader(MqttMessageType.UNSUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, 0x02);
        MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(messageId);
        MqttUnsubscribePayload mqttUnsubscribeMessage = new MqttUnsubscribePayload(topics);
        return new MqttUnsubscribeMessage(mqttFixedHeader, variableHeader, mqttUnsubscribeMessage);
    }

    /**
     *
     * @param clientId
     * @param willTopic
     * @param willMessage
     * @param username
     * @param password
     * @param isUsername
     * @param isPassword
     * @param hasWillRetain
     * @param isWill
     * @param willQos
     * @param hasCleanSession
     * @return
     */
    public static MqttConnectMessage buildConnect(String clientId, String willTopic, String willMessage,
        String username, String password, boolean isUsername, boolean isPassword, boolean hasWillRetain, boolean isWill,
        int willQos, boolean hasCleanSession, int heart) {
        MqttConnectVariableHeader mqttConnectVariableHeader =
            new MqttConnectVariableHeader(MqttVersion.MQTT_3_1_1.protocolName(), MqttVersion.MQTT_3_1_1.protocolLevel(),
                isUsername, isPassword, hasWillRetain, willQos, isWill, hasCleanSession, heart);
        MqttConnectPayload mqttConnectPayload = new MqttConnectPayload(clientId, willTopic,
            isWill ? willMessage.getBytes() : null, username, isPassword ? password.getBytes() : null);
        MqttFixedHeader mqttFixedHeader =
            new MqttFixedHeader(MqttMessageType.CONNECT, false, MqttQoS.AT_MOST_ONCE, false, 10);
        return new MqttConnectMessage(mqttFixedHeader, mqttConnectVariableHeader, mqttConnectPayload);
    }

}
