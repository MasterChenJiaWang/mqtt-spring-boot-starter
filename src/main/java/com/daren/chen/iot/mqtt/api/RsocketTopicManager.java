package com.daren.chen.iot.mqtt.api;

import java.util.List;

/**
 * 主题管理器
 */
public interface RsocketTopicManager {

    /**
     * 根据topic 获取所有连接
     *
     * @param topic
     *            主题
     * @return
     */
    List<TransportConnection> getConnectionsByTopic(String topic);

    /**
     * 添加主题
     *
     * @param topic
     *            主题
     * @param connection
     *            连接
     */
    void addTopicConnection(String topic, TransportConnection connection);

    /**
     * 删除主题
     *
     * @param topic
     *            主题
     * @param connection
     *            连接
     */
    void deleteTopicConnection(String topic, TransportConnection connection);

}
