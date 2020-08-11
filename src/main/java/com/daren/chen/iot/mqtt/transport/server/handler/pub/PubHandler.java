package com.daren.chen.iot.mqtt.transport.server.handler.pub;

import java.time.Duration;

import com.daren.chen.iot.mqtt.api.MqttMessageApi;
import com.daren.chen.iot.mqtt.api.RsocketConfiguration;
import com.daren.chen.iot.mqtt.api.TransportConnection;
import com.daren.chen.iot.mqtt.common.message.TransportMessage;
import com.daren.chen.iot.mqtt.config.RsocketServerConfig;
import com.daren.chen.iot.mqtt.transport.DirectHandler;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 发布消息处理器
 */
@Slf4j
public class PubHandler implements DirectHandler {

    /**
     * QoS 0 无响应 QoS 1 PUBACK报文 QoS 2 PUBREC报文
     *
     * @param message
     * @param connection
     * @param config
     */
    @Override
    public void handler(MqttMessage message, TransportConnection connection, RsocketConfiguration config) {
        RsocketServerConfig serverConfig = (RsocketServerConfig)config;
        MqttFixedHeader header = message.fixedHeader();
        switch (header.messageType()) {
            // 发布消息
            case CONNECT:
                break;
            case CONNACK:
                break;
            case PUBLISH:
                MqttPublishMessage mqttPublishMessage = (MqttPublishMessage)message;
                MqttPublishVariableHeader variableHeader = mqttPublishMessage.variableHeader();
                ByteBuf byteBuf = mqttPublishMessage.payload();
                byte[] bytes = copyByteBuf(byteBuf);
                String topicName = variableHeader.topicName();
                log.info("server pub accept  topic{} message {}", variableHeader.topicName(), new String(bytes));
                // 保留消息
                if (header.isRetain()) {
                    // 调用保留消息处理器
                    serverConfig.getMessageHandler().saveRetain(header.isDup(), header.isRetain(),
                        header.qosLevel().value(), topicName, bytes);
                }
                switch (header.qosLevel()) {
                    case AT_MOST_ONCE:
                        // 过滤掉本身和已经关闭的dispose
                        serverConfig.getTopicManager().getConnectionsByTopic(topicName).stream()
                            .filter(c -> !connection.equals(c) && !c.isDispose())
                            .forEach(c -> c.sendMessage(false, header.qosLevel(), header.isRetain(), topicName, bytes)
                                .subscribe());
                        break;
                    case AT_LEAST_ONCE:
                        MqttPubAckMessage mqttPubAckMessage = MqttMessageApi.buildPuback(header.isDup(),
                            header.qosLevel(), header.isRetain(), variableHeader.packetId()); // back
                        connection.write(mqttPubAckMessage).subscribe();
                        serverConfig.getTopicManager().getConnectionsByTopic(topicName).stream()
                            .filter(c -> !connection.equals(c) && !c.isDispose()).forEach(
                                c -> c.sendMessageRetry(false, header.qosLevel(), header.isRetain(), topicName, bytes)
                                    .subscribe());
                        break;
                    case EXACTLY_ONCE:
                        int messageId = variableHeader.packetId();
                        MqttPubAckMessage mqttPubRecMessage = MqttMessageApi.buildPubRec(messageId);
                        connection.write(mqttPubRecMessage).subscribe(); // send rec
                        connection.addDisposable(messageId,
                            Mono.fromRunnable(() -> connection.write(MqttMessageApi.buildPubRel(messageId)).subscribe())
                                .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe()); // retry
                        TransportMessage transportMessage = TransportMessage.builder().isRetain(header.isRetain())
                            .isDup(false).topic(topicName).message(bytes).qos(header.qosLevel().value()).build();
                        connection.saveQos2Message(messageId, transportMessage);
                        break;
                    case FAILURE:
                        log.error(" publish FAILURE {} {} ", header, variableHeader);
                        break;
                    default:
                        break;
                }
                break;
            // 发布确认报文 （QoS 1）
            case PUBACK:
                MqttMessageIdVariableHeader back = (MqttMessageIdVariableHeader)message.variableHeader();
                connection.cancelDisposable(back.messageId());
                break;
            // 发布收到报文 （QoS 2，第一步）
            case PUBREC:
                MqttMessageIdVariableHeader recVh = (MqttMessageIdVariableHeader)message.variableHeader();
                int id = recVh.messageId();
                connection.cancelDisposable(id);
                connection.write(MqttMessageApi.buildPubRel(id)).subscribe(); // send rel
                connection.addDisposable(id,
                    Mono.fromRunnable(() -> connection.write(MqttMessageApi.buildPubRel(id)).subscribe())
                        .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe()); // retry
                break;
            // 发布释放（QoS 2，第二步）
            case PUBREL:
                MqttMessageIdVariableHeader rel = (MqttMessageIdVariableHeader)message.variableHeader();
                int messageId = rel.messageId();
                connection.cancelDisposable(messageId); // cancel replay rec
                MqttPubAckMessage mqttPubRecMessage = MqttMessageApi.buildPubComp(messageId);
                connection.write(mqttPubRecMessage).subscribe(); // send comp
                connection.getAndRemoveQos2Message(messageId)
                    .ifPresent(msg -> serverConfig.getTopicManager().getConnectionsByTopic(msg.getTopic()).stream()
                        .filter(c -> !connection.equals(c) && !c.isDispose())
                        .forEach(c -> c.sendMessageRetry(false, MqttQoS.valueOf(msg.getQos()), header.isRetain(),
                            msg.getTopic(), msg.getMessage()).subscribe()));
                break;
            // 发布完成（QoS 2，第三步）
            case PUBCOMP:
                MqttMessageIdVariableHeader compVh = (MqttMessageIdVariableHeader)message.variableHeader();
                connection.cancelDisposable(compVh.messageId());
                break;

            case SUBSCRIBE:
                break;
            case SUBACK:
                break;
            case UNSUBSCRIBE:
                break;
            case UNSUBACK:
                break;
            case PINGREQ:
                break;
            case PINGRESP:
                break;
            case DISCONNECT:
                break;
            default:
                break;
        }
    }

    private byte[] copyByteBuf(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        return bytes;
    }
}
