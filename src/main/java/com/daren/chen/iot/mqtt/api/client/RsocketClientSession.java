package com.daren.chen.iot.mqtt.api.client;

import java.util.List;
import java.util.function.BiConsumer;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

public interface RsocketClientSession extends Disposable {

    /**
     *
     * @param topic
     * @param message
     * @param retained
     * @param qos
     * @return
     */
    Mono<Void> pub(String topic, byte[] message, boolean retained, int qos);

    /**
     *
     * @param topic
     * @param message
     * @return
     */
    Mono<Void> pub(String topic, byte[] message);

    /**
     *
     * @param topic
     * @param message
     * @param qos
     * @return
     */
    Mono<Void> pub(String topic, byte[] message, int qos);

    /**
     *
     * @param topic
     * @param message
     * @param retained
     * @return
     */
    Mono<Void> pub(String topic, byte[] message, boolean retained);

    /**
     *
     * @param subMessages
     * @return
     */
    Mono<Void> sub(String... subMessages);

    /**
     *
     * @param subMessages
     * @param qos
     * @return
     */
    Mono<Void> sub(String subMessages, int qos);

    /**
     *
     * @param topics
     * @return
     */
    Mono<Void> unsub(List<String> topics);

    /**
     *
     * @param topicsLike
     * @return
     */
    Mono<Void> unsubLike(String topicsLike);

    /**
     *
     * @return
     */
    Mono<Void> unsub();

    /**
     *
     * @param messageAcceptor
     * @return
     */
    Mono<Void> messageAcceptor(BiConsumer<String, byte[]> messageAcceptor);

    /**
     *
     */
    void initHandler();

    /**
     *
     * @return
     */
    List<String> getAllTopicList();

    /**
     * 模糊查询
     *
     * @param topicLike
     * @return
     */
    List<String> getTopicLikeList(String topicLike);

    /**
     *
     * @param topic
     * @return
     */
    String getTopic(String topic);
}
