package com.daren.chen.iot.mqtt.transport.server.handler.sub;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import com.daren.chen.iot.mqtt.api.MqttMessageApi;
import com.daren.chen.iot.mqtt.api.RsocketConfiguration;
import com.daren.chen.iot.mqtt.api.RsocketTopicManager;
import com.daren.chen.iot.mqtt.api.TransportConnection;
import com.daren.chen.iot.mqtt.config.RsocketServerConfig;
import com.daren.chen.iot.mqtt.transport.DirectHandler;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 订阅处理器
 */
@Slf4j
public class SubHandler implements DirectHandler {

    @Override
    public void handler(MqttMessage message, TransportConnection connection, RsocketConfiguration config) {
        RsocketServerConfig serverConfig = (RsocketServerConfig)config;
        MqttFixedHeader header = message.fixedHeader();
        switch (header.messageType()) {
            // 订阅主题
            case SUBSCRIBE:
                MqttSubscribeMessage subscribeMessage = (MqttSubscribeMessage)message;
                int messageId = subscribeMessage.variableHeader().messageId();
                List<Integer> grantedqoslevels = subscribeMessage.payload().topicSubscriptions().stream()
                    .map(m -> m.qualityOfService().value()).collect(Collectors.toList());
                MqttSubAckMessage mqttSubAckMessage = MqttMessageApi.buildSubAck(messageId, grantedqoslevels);
                connection.write(mqttSubAckMessage).subscribe();

                RsocketTopicManager topicManager = serverConfig.getTopicManager();
                subscribeMessage.payload().topicSubscriptions().forEach(m -> {
                    String topic = m.topicName();
                    topicManager.addTopicConnection(topic, connection);
                    serverConfig.getMessageHandler().getRetain(topic).ifPresent(messages -> {
                        byte[] copyByteBuf = messages.getCopyByteBuf();
                        String topicName = messages.getTopicName();
                        log.info("server sub accept  topic{} message {}", topicName, new String(copyByteBuf));
                        if (messages.getQos() == 0) {
                            connection
                                .write(MqttMessageApi.buildPub(messages.isDup(), MqttQoS.valueOf(messages.getQos()),
                                    messages.isRetain(), 1, topicName, Unpooled.wrappedBuffer(copyByteBuf)))
                                .subscribe();
                        } else {
                            int id = connection.messageId();
                            connection
                                .addDisposable(
                                    id, Mono
                                        .fromRunnable(
                                            () -> connection
                                                .write(
                                                    MqttMessageApi.buildPub(true, header.qosLevel(), header.isRetain(),
                                                        id, topicName, Unpooled.wrappedBuffer(copyByteBuf)))
                                                .subscribe())
                                        .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe()); // retry
                            MqttPublishMessage publishMessage = MqttMessageApi.buildPub(false, header.qosLevel(),
                                header.isRetain(), id, topicName, Unpooled.wrappedBuffer(copyByteBuf)); // pub
                            connection.write(publishMessage).subscribe();
                        }
                    });
                });
                break;
            case UNSUBSCRIBE:
                MqttUnsubscribeMessage mqttUnsubscribeMessage = (MqttUnsubscribeMessage)message;
                MqttUnsubAckMessage mqttUnsubAckMessage =
                    MqttMessageApi.buildUnsubAck(mqttUnsubscribeMessage.variableHeader().messageId());
                connection.write(mqttUnsubAckMessage).subscribe();
                mqttUnsubscribeMessage.payload().topics()
                    .forEach(m -> serverConfig.getTopicManager().deleteTopicConnection(m, connection));
                break;
            default:
                break;
        }
    }
}
