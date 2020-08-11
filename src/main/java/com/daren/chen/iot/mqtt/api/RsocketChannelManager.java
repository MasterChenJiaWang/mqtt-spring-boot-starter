package com.daren.chen.iot.mqtt.api;

import java.util.List;

/**
 *
 */
public interface RsocketChannelManager {

    /**
     *
     * @return
     */
    List<TransportConnection> getConnections();

    /**
     *
     * @param connection
     */
    void addConnections(TransportConnection connection);

    /**
     *
     * @param connection
     */
    void removeConnections(TransportConnection connection);

    /**
     *
     * @param deviceId
     * @param connection
     */
    void addDeviceId(String deviceId, TransportConnection connection);

    /**
     *
     * @param deviceId
     */
    void removeDeviceId(String deviceId);

    /**
     *
     * @param deviceId
     * @return
     */
    TransportConnection getRemoveDeviceId(String deviceId);

    /**
     *
     * @param deviceId
     * @return
     */
    boolean checkDeviceId(String deviceId);
}
