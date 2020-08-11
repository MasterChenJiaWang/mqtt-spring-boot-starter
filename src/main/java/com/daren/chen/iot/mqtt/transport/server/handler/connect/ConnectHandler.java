package com.daren.chen.iot.mqtt.transport.server.handler.connect;

import java.util.Optional;

import com.daren.chen.iot.mqtt.api.AttributeKeys;
import com.daren.chen.iot.mqtt.api.MqttMessageApi;
import com.daren.chen.iot.mqtt.api.RsocketChannelManager;
import com.daren.chen.iot.mqtt.api.RsocketConfiguration;
import com.daren.chen.iot.mqtt.api.TransportConnection;
import com.daren.chen.iot.mqtt.common.connection.WillMessage;
import com.daren.chen.iot.mqtt.config.RsocketServerConfig;
import com.daren.chen.iot.mqtt.transport.DirectHandler;

import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectPayload;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.Attribute;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;

/**
 * 连接处理器
 */
@Slf4j
public class ConnectHandler implements DirectHandler {

    /**
     *
     * @param message
     * @param connection
     * @param config
     */
    @Override
    public void handler(MqttMessage message, TransportConnection connection, RsocketConfiguration config) {
        RsocketServerConfig serverConfig = (RsocketServerConfig)config;
        switch (message.fixedHeader().messageType()) {
            case CONNECT:
                MqttConnectMessage connectMessage = (MqttConnectMessage)message;
                MqttConnectVariableHeader connectVariableHeader = connectMessage.variableHeader();
                MqttConnectPayload mqttConnectPayload = connectMessage.payload();
                RsocketChannelManager channelManager = serverConfig.getChannelManager();
                String clientId = mqttConnectPayload.clientIdentifier();
                // 设备id存在 就回写连接响应ack
                if (channelManager.checkDeviceId(clientId)) {
                    connection
                        .write(MqttMessageApi
                            .buildConnectAck(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED))
                        .subscribe(val -> {
                            log.info("val:{}", val);
                        }, error -> {
                            log.error("error:{}", error);
                        });
                    connection.dispose();
                } else {
                    // 有密码和用户名
                    if (connectVariableHeader.hasPassword() && connectVariableHeader.hasUserName()) {
                        // 检查用户名和密码
                        if (serverConfig.getAuth().apply(mqttConnectPayload.userName(),
                            mqttConnectPayload.passwordInBytes() == null ? null
                                : new String(mqttConnectPayload.passwordInBytes(), CharsetUtil.UTF_8))) {
                            connectSuccess(connection, serverConfig.getChannelManager(), clientId);
                        } else {
                            // 用户名 密码错误
                            connection
                                .write(MqttMessageApi.buildConnectAck(
                                    MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD))
                                .subscribe(val -> {
                                    log.info("val:{}", val);
                                }, error -> {
                                    log.error("error:{}", error);
                                });
                        }
                        // 有遗嘱 就保存遗嘱信息
                        if (connectVariableHeader.isWillFlag()) {
                            saveWill(connection, mqttConnectPayload.willTopic(), connectVariableHeader.isWillRetain(),
                                mqttConnectPayload.willMessageInBytes(), connectVariableHeader.willQos());
                        }
                    } else {
                        connectSuccess(connection, channelManager, clientId);
                        if (connectVariableHeader.isWillFlag()) {
                            saveWill(connection, mqttConnectPayload.willTopic(), connectVariableHeader.isWillRetain(),
                                mqttConnectPayload.willMessageInBytes(), connectVariableHeader.willQos());
                        }
                    }
                    break;
                }
                break;
            case DISCONNECT:
                serverConfig.getChannelManager().removeConnections(connection);
                connection.dispose();
                break;
            default:
                break;
        }
    }

    /**
     * 保存遗嘱 消息
     *
     * @param connection
     * @param willTopic
     * @param willRetain
     * @param willMessage
     * @param qoS
     */
    private void saveWill(TransportConnection connection, String willTopic, boolean willRetain, byte[] willMessage,
        int qoS) {
        WillMessage ws = new WillMessage(qoS, willTopic, willMessage, willRetain);
        // 设置device Id
        connection.getConnection().channel().attr(AttributeKeys.wILLMESSAGE).set(ws);
    }

    /**
     * 连接成功
     *
     * @param connection
     * @param channelManager
     * @param deviceId
     *            设备id
     */
    private void connectSuccess(TransportConnection connection, RsocketChannelManager channelManager, String deviceId) {
        // 设置device Id
        connection.getConnection().channel().attr(AttributeKeys.deviceId).set(deviceId);
        channelManager.addDeviceId(deviceId, connection);
        // 取消关闭连接
        Optional.ofNullable(connection.getConnection().channel().attr(AttributeKeys.closeConnection))
            .map(Attribute::get).ifPresent(Disposable::dispose);
        channelManager.addConnections(connection);
        connection.write(MqttMessageApi.buildConnectAck(MqttConnectReturnCode.CONNECTION_ACCEPTED)).subscribe();

    }
}
