package com.daren.chen.iot.mqtt.api.client;

/**
 * 消息处理器
 *
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/6/28 15:43
 */
public interface ClientMessageProcessor {

    /**
     *
     * @param receive
     * @return
     */
    default byte[] processor(byte[] receive) {
        return receive;
    }

}
