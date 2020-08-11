package com.daren.chen.iot.mqtt.transport.client.handler.pub;

import java.time.Duration;

import com.daren.chen.iot.mqtt.api.MqttMessageApi;
import com.daren.chen.iot.mqtt.api.RsocketConfiguration;
import com.daren.chen.iot.mqtt.api.TransportConnection;
import com.daren.chen.iot.mqtt.common.message.TransportMessage;
import com.daren.chen.iot.mqtt.config.RsocketClientConfig;
import com.daren.chen.iot.mqtt.transport.DirectHandler;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 发布处理器
 */
@Slf4j
public class PubHandler implements DirectHandler {

    @Override
    public void handler(MqttMessage message, TransportConnection connection, RsocketConfiguration config) {
        RsocketClientConfig clientConfig = (RsocketClientConfig)config;
        MqttFixedHeader header = message.fixedHeader();
        switch (header.messageType()) {
            case PUBLISH:
                MqttPublishMessage mqttPublishMessage = (MqttPublishMessage)message;
                MqttPublishVariableHeader variableHeader = mqttPublishMessage.variableHeader();
                ByteBuf byteBuf = mqttPublishMessage.payload();
                byte[] bytes = copyByteBuf(byteBuf);
                log.info("client pub accept  topic{} message {}", variableHeader.topicName(), new String(bytes));
                switch (header.qosLevel()) {
                    case AT_MOST_ONCE:
                        clientConfig.getMessageAcceptor().accept(variableHeader.topicName(), bytes);
                        break;
                    case AT_LEAST_ONCE:
                        clientConfig.getMessageAcceptor().accept(variableHeader.topicName(), bytes);
                        MqttPubAckMessage mqttPubAckMessage = MqttMessageApi.buildPuback(header.isDup(),
                            header.qosLevel(), header.isRetain(), variableHeader.packetId()); // back
                        connection.write(mqttPubAckMessage).subscribe();
                        break;
                    case EXACTLY_ONCE:
                        int messageId = variableHeader.packetId();
                        // 重试
                        connection.addDisposable(messageId,
                            Mono.fromRunnable(() -> connection.write(MqttMessageApi.buildPubRec(messageId)).subscribe())
                                .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe());
                        //
                        connection.write(MqttMessageApi.buildPubRec(messageId)).subscribe();
                        // send rec
                        TransportMessage transportMessage = TransportMessage.builder().isRetain(header.isRetain())
                            .isDup(false).topic(variableHeader.topicName()).message(bytes)
                            .qos(header.qosLevel().value()).build();
                        connection.saveQos2Message(messageId, transportMessage);
                        break;
                    case FAILURE:
                        log.error(" publish FAILURE {} {} ", header, variableHeader);
                        break;
                    default:
                        break;
                }
                break;
            case PUBACK:
                MqttMessageIdVariableHeader back = (MqttMessageIdVariableHeader)message.variableHeader();
                connection.cancelDisposable(back.messageId());
                break;
            case PUBREC:
                MqttMessageIdVariableHeader recVh = (MqttMessageIdVariableHeader)message.variableHeader();
                int id = recVh.messageId();
                connection.cancelDisposable(id);
                // 重试
                connection.addDisposable(id,
                    Mono.fromRunnable(() -> connection.write(MqttMessageApi.buildPubRel(id)).subscribe())
                        .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe());
                // send rel
                connection.write(MqttMessageApi.buildPubRel(id)).subscribe();
                break;
            case PUBREL:
                MqttMessageIdVariableHeader b = (MqttMessageIdVariableHeader)message.variableHeader();
                int messageId = b.messageId();
                // 取消重试
                connection.cancelDisposable(messageId);
                // send comp
                connection.write(MqttMessageApi.buildPubComp(messageId)).subscribe();
                connection.getAndRemoveQos2Message(messageId)
                    .ifPresent(msg -> clientConfig.getMessageAcceptor().accept(msg.getTopic(), msg.getMessage()));
                break;
            case PUBCOMP:
                MqttMessageIdVariableHeader compVh = (MqttMessageIdVariableHeader)message.variableHeader();
                connection.cancelDisposable(compVh.messageId());
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
