package com.daren.chen.iot.mqtt.api.server.path;

import java.util.List;
import java.util.Optional;

import com.daren.chen.iot.mqtt.api.TransportConnection;

/**
 * 主题管理器
 */
public class DefaultTopicManager {

    private final TopicFilter<TransportConnection> pathMap = new TopicFilter<>();

    /**
     *
     * @param topic
     * @return
     */
    public Optional<List<TransportConnection>> getTopicConnection(String topic) {
        try {
            return Optional.ofNullable(pathMap.getData(topic));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public synchronized void addTopicConnection(String topic, TransportConnection connection) {
        pathMap.addTopic(topic, connection);
    }

    public synchronized void deleteTopicConnection(String topic, TransportConnection connection) {
        pathMap.delete(topic, connection);
    }
}
