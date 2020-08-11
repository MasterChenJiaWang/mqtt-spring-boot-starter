package com.daren.chen.iot.mqtt.transport.client;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.springframework.util.StringUtils;

import com.daren.chen.iot.mqtt.api.client.RsocketClientSession;
import com.daren.chen.iot.mqtt.common.annocation.ProtocolType;
import com.daren.chen.iot.mqtt.config.RsocketClientConfig;

import io.netty.handler.codec.mqtt.MqttQoS;
import reactor.core.publisher.Mono;

public class TransportClient {

    private static RsocketClientConfig config;

    private static TransportClientFactory transportFactory;

    private static RsocketClientConfig.Options options;

    private TransportClient() {

    }

    public static class TransportBuilder {

        public TransportBuilder() {
            config = new RsocketClientConfig();
            transportFactory = new TransportClientFactory();
            options = config.new Options();
        }

        public TransportBuilder(String ip, int port) {
            this();
            config = new RsocketClientConfig();
            config.setIp(ip);
            config.setPort(port);
        }

        public TransportBuilder protocol(ProtocolType protocolType) {
            config.setProtocol(protocolType.name());
            return this;
        }

        public TransportBuilder heart(int heart) {
            config.setHeart(heart);
            return this;
        }

        public TransportBuilder subTopics(List<String> topics) {
            config.setSubTopics(topics);
            return this;
        }

        public TransportBuilder onClose(Runnable onClose) {
            config.setOnClose(onClose);
            return this;
        }

        public TransportBuilder exception(Consumer<Throwable> exceptionConsumer) {
            config.setThrowableConsumer(exceptionConsumer);
            return this;
        }

        public TransportBuilder ssl(boolean ssl) {
            config.setSsl(ssl);
            return this;
        }

        public TransportBuilder messageAcceptor(BiConsumer<String, byte[]> messageAcceptor) {
            config.setMessageAcceptor(messageAcceptor);
            return this;
        }

        public TransportBuilder clientId(String clientId) {
            options.setClientIdentifier(StringUtils.isEmpty(clientId) ? UUID.randomUUID().toString() : clientId);
            return this;
        }

        public TransportBuilder username(String username) {
            options.setUserName(username);
            options.setHasUserName(true);
            return this;
        }

        public TransportBuilder password(String password) {
            options.setPassword(password);
            options.setHasPassword(true);
            return this;
        }

        public TransportBuilder willTopic(String willTopic) {
            if (willTopic == null) {
                return this;
            }
            options.setWillTopic(willTopic);
            options.setHasWillFlag(true);
            return this;
        }

        public TransportBuilder willMessage(String willMessage) {
            if (willMessage == null) {
                return this;
            }
            options.setWillMessage(willMessage);
            options.setHasWillFlag(true);
            return this;
        }

        public TransportBuilder willQos(MqttQoS qoS) {
            if (qoS == null) {
                return this;
            }
            options.setWillQos(qoS.value());
            options.setHasWillFlag(true);
            return this;
        }

        public TransportBuilder log(boolean log) {
            config.setLog(log);
            return this;
        }

        public Mono<RsocketClientSession> connect() {
            config.setOptions(options);
            config.checkConfig();
            return transportFactory.connect(config);
        }

    }

    public static TransportBuilder create(String ip, int port) {
        return new TransportBuilder(ip, port);
    }

    public TransportBuilder create() {
        return new TransportBuilder();
    }

}
