package com.daren.chen.iot.mqtt.transport.server;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.daren.chen.iot.mqtt.api.RsocketMessageHandler;
import com.daren.chen.iot.mqtt.api.RsocketTopicManager;
import com.daren.chen.iot.mqtt.api.server.RsocketServerSession;
import com.daren.chen.iot.mqtt.common.annocation.ProtocolType;
import com.daren.chen.iot.mqtt.config.RsocketServerConfig;

import reactor.core.publisher.Mono;

public class TransportServer {
    private static RsocketServerConfig config;

    private static TransportServerFactory transportFactory;

    private TransportServer() {}

    public static class TransportBuilder {

        public TransportBuilder() {
            config = new RsocketServerConfig();
            transportFactory = new TransportServerFactory();
        }

        public TransportBuilder(String ip, int port) {
            this();
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

        public TransportBuilder ssl(boolean ssl) {
            config.setSsl(ssl);
            return this;
        }

        public TransportBuilder log(boolean log) {
            config.setLog(log);
            return this;
        }

        public TransportBuilder keepAlive(boolean isKeepAlive) {
            config.setKeepAlive(isKeepAlive);
            return this;
        }

        public TransportBuilder noDelay(boolean noDelay) {
            config.setNoDelay(noDelay);
            return this;
        }

        public TransportBuilder backlog(int length) {
            config.setBacklog(length);
            return this;
        }

        public TransportBuilder sendBufSize(int size) {
            config.setSendBufSize(size);
            return this;
        }

        public TransportBuilder revBufSize(int size) {
            config.setRevBufSize(size);
            return this;
        }

        public TransportBuilder auth(BiFunction<String, String, Boolean> auth) {
            config.setAuth(auth);
            return this;
        }

        public TransportBuilder messageHandler(RsocketMessageHandler messageHandler) {
            Optional.ofNullable(messageHandler).ifPresent(config::setMessageHandler);
            return this;
        }

        public TransportBuilder exception(Consumer<Throwable> exceptionConsumer) {
            Optional.ofNullable(exceptionConsumer).ifPresent(config::setThrowableConsumer);
            return this;
        }

        public TransportBuilder topicManager(RsocketTopicManager rsocketTopicManager) {
            Optional.ofNullable(rsocketTopicManager).ifPresent(config::setTopicManager);
            return this;
        }

        public Mono<RsocketServerSession> start() {
            config.checkConfig();
            return transportFactory.start(config);
        }
    }

    public static TransportBuilder create(String ip, int port) {
        return new TransportBuilder(ip, port);
    }

    public static TransportBuilder create() {
        return new TransportBuilder();
    }
}
