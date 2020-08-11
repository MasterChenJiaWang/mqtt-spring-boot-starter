package com.daren.chen.iot.mqtt.api.server.handler;

import java.util.List;

import com.daren.chen.iot.mqtt.api.RsocketTopicManager;
import com.daren.chen.iot.mqtt.api.TransportConnection;
import com.daren.chen.iot.mqtt.api.server.path.DefaultTopicManager;
import com.google.common.collect.Lists;

/**
 * 内存主题管理器
 *
 * 默认 内存管理器
 *
 */
public class MemoryTopicManager implements RsocketTopicManager {

    /**
     * 主题管理器 默认用 DefaultTopicManager
     */
    private final DefaultTopicManager topicManager = new DefaultTopicManager();

    /**
     * 获取主题连接
     *
     * @param topic
     * @return
     */
    @Override
    public List<TransportConnection> getConnectionsByTopic(String topic) {
        return topicManager.getTopicConnection(topic).orElse(Lists.newArrayList());
    }

    /**
     * 添加主题连接
     *
     * @param topic
     * @param connection
     */
    @Override
    public void addTopicConnection(String topic, TransportConnection connection) {
        topicManager.addTopicConnection(topic, connection);
    }

    /**
     * 删除主题连接
     *
     * @param topic
     * @param connection
     */
    @Override
    public void deleteTopicConnection(String topic, TransportConnection connection) {
        topicManager.deleteTopicConnection(topic, connection);
    }

}
