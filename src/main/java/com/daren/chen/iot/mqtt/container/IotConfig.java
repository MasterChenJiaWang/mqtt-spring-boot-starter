package com.daren.chen.iot.mqtt.container;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.daren.chen.iot.mqtt.common.annocation.ProtocolType;
import com.daren.chen.iot.mqtt.container.topic.MqttPayloadDecodeMethod;

import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.Data;

/**
 * @author 曾帅
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "unittec.iot.mqtt")
public class IotConfig {

    /**
     * 服务端
     */
    private Server server;

    /**
     * 客户端
     */
    private Client client;

    @Data
    public static class Server {
        /**
         *
         */
        private boolean enable;

        /**
         *
         */
        private String host;
        /**
         *
         */
        private int port;
        /**
         *
         */
        private boolean log;
        /**
         * 消息类型
         */
        private ProtocolType protocol;
        /**
         *
         */
        private int heart;
        /**
         *
         */
        private boolean ssl;

        /**
         * 发送缓冲区大小 默认 32k
         */
        private int sendBufSize = 32 * 1024;
        /**
         * 接收缓冲区大小 默认 32k
         */
        private int revBufSize = 32 * 1024;

        /**
         * Socket参数，连接保活，默认值为False。启用该功能时，TCP会主动探测空闲连接的有效性。可以将此功能视为TCP的心跳机制， 需要注意的是：默认的心跳间隔是7200s即2小时。Netty默认关闭该功能。
         */
        private boolean keepAlive = false;
        /**
         * Socket参数，立即发送数据，默认值为True（Netty默认为True而操作系统默认为False）。该值设置Nagle算法的启用， 该算法将小的碎片数据连接成更大的报文来最小化所发送的报文的数量，
         * 如果需要发送一些较小的报文，则需要禁用该算法。 Netty默认禁用该算法，从而最小化报文传输延时
         */
        private boolean noDelay = true;

    }

    @Data
    public static class Client {
        private boolean enable;

        private String ip;

        private int port;

        private ProtocolType protocol;

        private int heart;

        private boolean log;

        private boolean ssl;
        /**
         * 客户端 启动 sub topic
         */
        private List<String> subTopics;
        /**
         * 发送缓冲区大小 默认 32k
         */
        private int sendBufSize = 32 * 1024;
        /**
         * 接收缓冲区大小 默认 32k
         */
        private int revBufSize = 32 * 1024;

        /**
         * Socket参数，连接保活，默认值为False。启用该功能时，TCP会主动探测空闲连接的有效性。可以将此功能视为TCP的心跳机制， 需要注意的是：默认的心跳间隔是7200s即2小时。Netty默认关闭该功能。
         */
        private boolean keepAlive = false;
        /**
         * Socket参数，立即发送数据，默认值为True（Netty默认为True而操作系统默认为False）。该值设置Nagle算法的启用， 该算法将小的碎片数据连接成更大的报文来最小化所发送的报文的数量，
         * 如果需要发送一些较小的报文，则需要禁用该算法。 Netty默认禁用该算法，从而最小化报文传输延时
         */
        private boolean noDelay = true;

        /**
         * 客户端支持解码配置，默认使用JSON
         */
        private MqttPayloadDecodeMethod decodeMethod = MqttPayloadDecodeMethod.JSON;

        /**
         *
         */
        private Consumer<Throwable> throwableConsumer;

        /**
         *
         */
        private BiConsumer<String, byte[]> messageAcceptor;

        /**
         *
         */
        private Runnable onClose = () -> {
        };

        /**
         *
         */
        private Options option;

        @Data
        public static class Options {

            private String clientIdentifier;

            private String willTopic;

            private String willMessage;

            private String userName;

            private String password;

            private boolean hasUserName;

            private boolean hasPassword;

            private boolean hasWillRetain;

            private MqttQoS willQos;

            private boolean hasWillFlag;

            private boolean hasCleanSession;

        }

    }

}
