package com.daren.chen.iot.mqtt.container;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/7/30 14:38
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
