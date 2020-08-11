package com.daren.chen.iot.mqtt.transport.client.connection;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.daren.chen.iot.mqtt.api.AttributeKeys;
import com.daren.chen.iot.mqtt.api.MqttMessageApi;
import com.daren.chen.iot.mqtt.api.TransportConnection;
import com.daren.chen.iot.mqtt.api.client.RsocketClientSession;
import com.daren.chen.iot.mqtt.config.RsocketClientConfig;
import com.daren.chen.iot.mqtt.transport.client.handler.ClientMessageRouter;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

@Slf4j
public class RsocketClientConnection implements RsocketClientSession {

    private final TransportConnection connection;

    private final RsocketClientConfig clientConfig;

    private final ClientMessageRouter clientMessageRouter;

    /**
     *
     * @param connection
     * @param clientConfig
     */
    public RsocketClientConnection(TransportConnection connection, RsocketClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.connection = connection;
        this.clientMessageRouter = new ClientMessageRouter(clientConfig);
        initHandler();
    }

    @Override
    public void initHandler() {
        addSubTopics();
        //
        RsocketClientConfig.Options options = clientConfig.getOptions();
        // 登录重试
        Disposable disposable = Mono
            .fromRunnable(() -> connection.write(MqttMessageApi.buildConnect(options.getClientIdentifier(),
                options.getWillTopic(), options.getWillMessage(), options.getUserName(), options.getPassword(),
                options.isHasUserName(), options.isHasPassword(), options.isHasWillRetain(), options.isHasWillFlag(),
                options.getWillQos(), options.isHasCleanSession(), clientConfig.getHeart())).subscribe())
            .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe(val -> {
                log.info("val:{}", val);
            }, throwable -> clientConfig.getThrowableConsumer().accept(throwable));
        // 登录
        connection
            .write(MqttMessageApi.buildConnect(options.getClientIdentifier(), options.getWillTopic(),
                options.getWillMessage(), options.getUserName(), options.getPassword(), options.isHasUserName(),
                options.isHasPassword(), options.isHasWillRetain(), options.isHasWillFlag(), options.getWillQos(),
                options.isHasCleanSession(), clientConfig.getHeart()))
            .doOnError(throwable -> log.error(throwable.getMessage())).subscribe(val -> {
                log.info("val:{}", val);
            }, throwable -> clientConfig.getThrowableConsumer().accept(throwable));
        // 保存 重试线程,用于在其他地方 关闭线程
        connection.getConnection().channel().attr(AttributeKeys.closeConnection).set(disposable);
        // 发送心跳
        connection.getConnection().onWriteIdle(clientConfig.getHeart(), () -> connection.sendPingReq().subscribe());
        // 发送心跳
        connection.getConnection().onReadIdle(clientConfig.getHeart() * 2, () -> connection.sendPingReq().subscribe());
        // 关闭 异常关闭
        connection.getConnection().onDispose(() -> clientConfig.getOnClose().run());
        // 收到消息 处理逻辑
        connection.receive(MqttMessage.class).subscribe(message -> clientMessageRouter.handler(message, connection));
        //
        connection.getConnection().channel().attr(AttributeKeys.clientConnectionAttributeKey).set(this);
        // 重连后 需要把以前订阅的主题重新订阅
        List<MqttTopicSubscription> mqttTopicSubscriptions = this.connection.getTopics().stream()
            .map(s -> new MqttTopicSubscription(s, MqttQoS.AT_MOST_ONCE)).collect(Collectors.toList());
        // // 订阅主题 重试线程 // retryPooledConnectionProvider
        if (mqttTopicSubscriptions.size() > 0) {
            int messageId = connection.messageId();
            connection.addDisposable(messageId,
                Mono.fromRunnable(
                    () -> connection.write(MqttMessageApi.buildSub(messageId, mqttTopicSubscriptions)).subscribe())
                    .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe());
            connection.write(MqttMessageApi.buildSub(messageId, mqttTopicSubscriptions)).subscribe();
        }
    }

    private void addSubTopics() {
        if (clientConfig.getSubTopics() != null && clientConfig.getSubTopics().size() > 0) {
            this.connection.getTopics().addAll(clientConfig.getSubTopics());
        }
    }

    @Override
    public List<String> getAllTopicList() {
        return this.connection.getTopics();
    }

    @Override
    public List<String> getTopicLikeList(String topicLike) {
        return this.connection.getTopics().stream().filter(s -> s.startsWith(topicLike)).collect(Collectors.toList());
    }

    @Override
    public String getTopic(String topic) {
        return this.connection.getTopics().stream().filter(s -> s.equals(topic)).findFirst().orElse("");
    }

    @Override
    public Mono<Void> pub(String topic, byte[] message, boolean retained, int qos) {
        int messageId = qos == 0 ? 1 : connection.messageId();
        MqttQoS mqttQoS = MqttQoS.valueOf(qos);
        switch (mqttQoS) {
            case AT_MOST_ONCE:
                return connection.write(MqttMessageApi.buildPub(false, MqttQoS.AT_MOST_ONCE, retained, messageId, topic,
                    Unpooled.wrappedBuffer(message)));
            case EXACTLY_ONCE:
            case AT_LEAST_ONCE:
                return Mono.fromRunnable(() -> {
                    connection.write(MqttMessageApi.buildPub(false, mqttQoS, retained, messageId, topic,
                        Unpooled.wrappedBuffer(message))).subscribe();
                    // 失败 重试线程
                    connection.addDisposable(messageId,
                        Mono.fromRunnable(() -> connection.write(MqttMessageApi.buildPub(true, mqttQoS, retained,
                            messageId, topic, Unpooled.wrappedBuffer(message))).subscribe())
                            .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe()); // retry
                });
            default:
                return Mono.empty();
        }
    }

    @Override
    public Mono<Void> pub(String topic, byte[] message) {
        return pub(topic, message, false, 0);
    }

    @Override
    public Mono<Void> pub(String topic, byte[] message, int qos) {
        return pub(topic, message, false, qos);
    }

    @Override
    public Mono<Void> pub(String topic, byte[] message, boolean retained) {
        return pub(topic, message, retained, 0);
    }

    @Override
    public Mono<Void> sub(String... subMessages) {
        this.connection.getTopics().addAll(Arrays.asList(subMessages));
        List<MqttTopicSubscription> topicSubscriptions = Arrays.stream(subMessages)
            .map(s -> new MqttTopicSubscription(s, MqttQoS.AT_MOST_ONCE)).collect(Collectors.toList());
        int messageId = connection.messageId();
        connection.addDisposable(messageId, Mono
            .fromRunnable(() -> connection.write(MqttMessageApi.buildSub(messageId, topicSubscriptions)).subscribe())
            .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe());
        return connection.write(MqttMessageApi.buildSub(messageId, topicSubscriptions));
    }

    /**
     *
     * @param subMessages
     * @param qos
     * @return
     */
    @Override
    public Mono<Void> sub(String subMessages, int qos) {
        this.connection.getTopics().add(subMessages);
        MqttQoS mqttQoS = MqttQoS.valueOf(qos);
        List<MqttTopicSubscription> topicSubscriptions = new ArrayList<>();
        topicSubscriptions.add(new MqttTopicSubscription(subMessages, mqttQoS));
        int messageId = connection.messageId();
        connection.addDisposable(messageId, Mono
            .fromRunnable(() -> connection.write(MqttMessageApi.buildSub(messageId, topicSubscriptions)).subscribe())
            .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe());
        return connection.write(MqttMessageApi.buildSub(messageId, topicSubscriptions));
    }

    @Override
    public Mono<Void> unsub(List<String> topics) {
        this.connection.getTopics().removeAll(topics);
        int messageId = connection.messageId();
        connection.addDisposable(messageId,
            Mono.fromRunnable(() -> connection.write(MqttMessageApi.buildUnSub(messageId, topics)).subscribe())
                .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe());
        return connection.write(MqttMessageApi.buildUnSub(messageId, topics));
    }

    @Override
    public Mono<Void> unsubLike(String topicsLike) {
        return unsub(
            this.connection.getTopics().stream().filter(s -> s.startsWith(topicsLike)).collect(Collectors.toList()));
    }

    @Override
    public Mono<Void> unsub() {
        return unsub(new ArrayList<>(this.connection.getTopics()));
    }

    @Override
    public Mono<Void> messageAcceptor(BiConsumer<String, byte[]> messageAcceptor) {
        return Mono.fromRunnable(() -> clientConfig.setMessageAcceptor(messageAcceptor));
    }

    @Override
    public void dispose() {
        connection.dispose();
    }
}
