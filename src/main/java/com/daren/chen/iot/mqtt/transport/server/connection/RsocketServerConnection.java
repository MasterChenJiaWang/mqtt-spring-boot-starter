package com.daren.chen.iot.mqtt.transport.server.connection;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import com.daren.chen.iot.mqtt.api.AttributeKeys;
import com.daren.chen.iot.mqtt.api.RsocketChannelManager;
import com.daren.chen.iot.mqtt.api.RsocketMessageHandler;
import com.daren.chen.iot.mqtt.api.RsocketTopicManager;
import com.daren.chen.iot.mqtt.api.TransportConnection;
import com.daren.chen.iot.mqtt.api.server.RsocketServerSession;
import com.daren.chen.iot.mqtt.api.server.handler.MemoryChannelManager;
import com.daren.chen.iot.mqtt.api.server.handler.MemoryMessageHandler;
import com.daren.chen.iot.mqtt.api.server.handler.MemoryTopicManager;
import com.daren.chen.iot.mqtt.config.RsocketServerConfig;
import com.daren.chen.iot.mqtt.transport.server.handler.ServerMessageRouter;

import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.Attribute;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.netty.Connection;
import reactor.netty.DisposableChannel;
import reactor.netty.DisposableServer;
import reactor.netty.NettyInbound;

/**
 *
 */
public class RsocketServerConnection implements RsocketServerSession {

    /**
     *
     */
    private final DisposableServer disposableServer;

    /**
     *
     */
    private final RsocketChannelManager channelManager;

    /**
     *
     */
    private final RsocketTopicManager topicManager;

    /**
     *
     */
    private RsocketMessageHandler rsocketMessageHandler;

    /**
     *
     */
    private final RsocketServerConfig config;
    /**
     *
     */
    private final ServerMessageRouter messageRouter;
    /**
     *
     */
    private final DisposableServer wsDisposableServer;

    public RsocketServerConnection(UnicastProcessor<TransportConnection> connections, DisposableServer server,
        DisposableServer wsDisposableServer, RsocketServerConfig config) {
        this.disposableServer = server;
        this.config = config;
        this.rsocketMessageHandler = Optional.ofNullable(config.getMessageHandler()).orElse(new MemoryMessageHandler());
        this.topicManager = Optional.ofNullable(config.getTopicManager()).orElse(new MemoryTopicManager());
        this.channelManager = Optional.ofNullable(config.getChannelManager()).orElse(new MemoryChannelManager());
        this.messageRouter = new ServerMessageRouter(config);
        this.wsDisposableServer = wsDisposableServer;
        connections.subscribe(this::subscribe);

    }

    /**
     * 订阅
     *
     * @param connection
     *            链接
     */
    private void subscribe(TransportConnection connection) {
        NettyInbound inbound = connection.getInbound();
        Connection c = connection.getConnection();
        // 定时关闭
        Disposable disposable = Mono.fromRunnable(c::dispose).delaySubscription(Duration.ofSeconds(10)).subscribe();
        // 设置connection
        c.channel().attr(AttributeKeys.connectionAttributeKey).set(connection);
        // 设置close
        c.channel().attr(AttributeKeys.closeConnection).set(disposable);
        // 心跳超时关闭
        connection.getConnection().onReadIdle(config.getHeart(), () -> connection.getConnection().dispose());
        // 关闭 执行的逻辑
        connection.getConnection().onDispose(() -> {
            // 发送will消息
            Optional.ofNullable(connection.getConnection().channel().attr(AttributeKeys.wILLMESSAGE))
                .map(Attribute::get).ifPresent(
                    willMessage -> Optional.ofNullable(topicManager.getConnectionsByTopic(willMessage.getTopicName()))
                        .ifPresent(connections -> connections.forEach(co -> {
                            MqttQoS qoS = MqttQoS.valueOf(willMessage.getQos());
                            switch (qoS) {
                                case AT_LEAST_ONCE:
                                    co.sendMessage(false, qoS, willMessage.isRetain(), willMessage.getTopicName(),
                                        willMessage.getCopyByteBuf()).subscribe();
                                    break;
                                case EXACTLY_ONCE:
                                case AT_MOST_ONCE:
                                    // 重试
                                    co.sendMessageRetry(false, qoS, willMessage.isRetain(), willMessage.getTopicName(),
                                        willMessage.getCopyByteBuf()).subscribe();
                                    break;
                                default:
                                    co.sendMessage(false, qoS, willMessage.isRetain(), willMessage.getTopicName(),
                                        willMessage.getCopyByteBuf()).subscribe();
                                    break;
                            }
                        })));
            // 删除链接
            channelManager.removeConnections(connection);
            // 删除topic订阅
            connection.getTopics().forEach(topic -> topicManager.deleteTopicConnection(topic, connection));
            // 设置device Id
            Optional.ofNullable(connection.getConnection().channel().attr(AttributeKeys.deviceId)).map(Attribute::get)
                .ifPresent(channelManager::removeDeviceId);
            connection.destory();
        });
        // 接收消息处理逻辑
        inbound.receiveObject().cast(MqttMessage.class)
            .subscribe(message -> messageRouter.handler(message, connection));
    }

    /**
     *
     * @return
     */
    @Override
    public Mono<List<TransportConnection>> getConnections() {
        return Mono.just(channelManager.getConnections());
    }

    /**
     *
     * @param clientId
     * @return
     */
    @Override
    public Mono<Void> closeConnect(String clientId) {
        return Mono.fromRunnable(() -> Optional.ofNullable(channelManager.getRemoveDeviceId(clientId))
            .ifPresent(TransportConnection::dispose));
    }

    /**
     *
     */
    @Override
    public void dispose() {
        disposableServer.dispose();
        Optional.ofNullable(wsDisposableServer).ifPresent(DisposableChannel::dispose);
    }
}
