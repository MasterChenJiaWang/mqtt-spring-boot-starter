package com.daren.chen.iot.mqtt.api.server.handler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.daren.chen.iot.mqtt.api.RsocketChannelManager;
import com.daren.chen.iot.mqtt.api.TransportConnection;

/**
 * 内存通道管理器
 */
public class MemoryChannelManager implements RsocketChannelManager {

    /**
     * 连接 集合
     */
    private final List<TransportConnection> connections = new CopyOnWriteArrayList<>();

    /**
     * 设备 连接 集合
     */
    private final Map<String, TransportConnection> connectionMap = new ConcurrentHashMap<>();

    /**
     *
     * @return
     */
    @Override
    public List<TransportConnection> getConnections() {
        return connections;
    }

    /**
     *
     * @param connection
     */
    @Override
    public void addConnections(TransportConnection connection) {
        connections.add(connection);
    }

    /**
     *
     * @param connection
     */
    @Override
    public void removeConnections(TransportConnection connection) {
        connections.remove(connection);
    }

    /**
     *
     * @param deviceId
     * @param connection
     */
    @Override
    public void addDeviceId(String deviceId, TransportConnection connection) {
        connectionMap.put(deviceId, connection);
    }

    /**
     *
     * @param deviceId
     */
    @Override
    public void removeDeviceId(String deviceId) {
        connectionMap.remove(deviceId);
    }

    /**
     *
     * @param deviceId
     * @return
     */
    @Override
    public TransportConnection getRemoveDeviceId(String deviceId) {
        return connectionMap.remove(deviceId);
    }

    /**
     * 检查设备ID 是否存在
     *
     * @param deviceId
     * @return
     */
    @Override
    public boolean checkDeviceId(String deviceId) {
        return connectionMap.containsKey(deviceId);
    }

}
