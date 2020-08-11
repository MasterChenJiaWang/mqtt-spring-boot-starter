package com.daren.chen.iot.mqtt.container;

/**
 * 异常处理
 */
public interface ExceptorAcceptor {

    /**
     *
     * @param throwable
     */
    void accept(Throwable throwable);

}
