package com.daren.chen.iot.mqtt.container.topic;

/**
 *
 * @param <T>
 */
public interface ITopicHandler<T> {
    /**
     *
     * @param data
     * @return
     */
    T decode(byte[] data);

    /**
     *
     * @param t
     * @return
     * @throws Exception
     */
    byte[] handle(T t) throws Exception;
}
