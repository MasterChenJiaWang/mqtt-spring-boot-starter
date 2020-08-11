package com.daren.chen.iot.mqtt.protocol.mqtt;

import java.util.Objects;
import java.util.Optional;

import com.daren.chen.iot.mqtt.api.AttributeKeys;
import com.daren.chen.iot.mqtt.api.RsocketConfiguration;
import com.daren.chen.iot.mqtt.api.TransportConnection;
import com.daren.chen.iot.mqtt.protocol.BaseProtocolTransport;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpClient;
import reactor.netty.tcp.TcpServer;

/**
 *
 */
@Slf4j
public class MqttTransport extends BaseProtocolTransport {

    public MqttTransport(MqttProtocol mqttProtocol) {
        super(mqttProtocol);
    }

    @Override
    public Mono<? extends DisposableServer> start(RsocketConfiguration config,
        UnicastProcessor<TransportConnection> connections) {
        return buildServer(config).doOnConnection(connection -> {
            protocol.getHandlers().forEach(connection::addHandlerLast);
            connections.onNext(new TransportConnection(connection));
        }).bind().doOnError(config.getThrowableConsumer());
    }

    private TcpServer buildServer(RsocketConfiguration config) {
        TcpServer server = TcpServer.create().port(config.getPort()).wiretap(config.isLog()).host(config.getIp())
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .option(ChannelOption.SO_KEEPALIVE, config.isKeepAlive())
            .option(ChannelOption.TCP_NODELAY, config.isNoDelay())
            .option(ChannelOption.SO_RCVBUF, config.getRevBufSize())
            .option(ChannelOption.SO_SNDBUF, config.getSendBufSize());
        return config.isSsl()
            ? server.secure(sslContextSpec -> sslContextSpec.sslContext(Objects.requireNonNull(buildContext())))
            : server;

    }

    private SslContext buildContext() {
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } catch (Exception e) {
            log.error("*******************************************************************ssl error: {}",
                e.getMessage());
        }
        return null;
    }

    @Override
    public Mono<TransportConnection> connect(RsocketConfiguration config) {
        try {
            return buildClient(config).connect().map(connection -> {
                Connection connection1 = connection;
                log.info(".................................链接成功了...................................");
                protocol.getHandlers().forEach(connection1::addHandler);
                TransportConnection transportConnection = new TransportConnection(connection);
                //
                connection.onDispose(() -> retryConnect(config, transportConnection));
                return transportConnection;
            });
        } catch (Exception e) {
            e.printStackTrace();
            config.getThrowableConsumer().accept(e);
            return Mono.error(e);
        }
    }

    private void retryConnect(RsocketConfiguration config, TransportConnection transportConnection) {
        log.info("断线重连中..............................................................");
        buildClient(config).connect().doOnError(config.getThrowableConsumer()).retry().cast(Connection.class)
            .subscribe(connection -> {
                Connection connection1 = connection;
                protocol.getHandlers().forEach(connection1::addHandler);
                Optional
                    .ofNullable(
                        transportConnection.getConnection().channel().attr(AttributeKeys.clientConnectionAttributeKey))
                    .map(Attribute::get).ifPresent(rsocketClientSession -> {
                        transportConnection.setConnection(connection);
                        transportConnection.setInbound(connection.inbound());
                        transportConnection.setOutbound(connection.outbound());
                        rsocketClientSession.initHandler();
                        log.info("..............................................................重连成功");
                    });

            });
    }

    private TcpClient buildClient(RsocketConfiguration config) {
        TcpClient client = TcpClient.create().port(config.getPort()).host(config.getIp())
            .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(1024 * 1024))
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .option(ChannelOption.SO_KEEPALIVE, config.isKeepAlive())
            .option(ChannelOption.TCP_NODELAY, config.isNoDelay())
            .option(ChannelOption.SO_RCVBUF, config.getRevBufSize())
            .option(ChannelOption.SO_SNDBUF, config.getSendBufSize()).wiretap(config.isLog())
            .doOnDisconnected(connection -> {
                log.info("buildClient 连接失败!!!!................");
            });
        try {
            SslContext sslClient =
                SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            return config.isSsl() ? client.secure(sslContextSpec -> sslContextSpec.sslContext(sslClient)) : client;
        } catch (Exception e) {
            config.getThrowableConsumer().accept(e);
            return client;
        }
    }

}
