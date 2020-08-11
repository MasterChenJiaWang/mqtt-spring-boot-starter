package com.daren.chen.iot.mqtt.container;// package com.daren.chen.iot.mqtt.container;

//
// import java.util.Arrays;
//
// import javax.annotation.Resource;
//
// import org.springframework.context.ApplicationContext;
//
// import com.daren.chen.iot.mqtt.api.client.RsocketClientSession;
// import com.daren.chen.iot.mqtt.container.topic.ITopicHandler;
//
// import lombok.extern.slf4j.Slf4j;
//
// @Slf4j
// public class ClientMessageAcceptor implements MessageAcceptor {
//
// @Resource
// private ApplicationContext applicationContext;
//
// @Override
// public void accept(String topic, byte[] message) {
// if (log.isDebugEnabled()) {
// log.debug("topic={},message={}", topic, Arrays.toString(message));
// }
// String[] topicArray = topic.split("/");
// Object bean = applicationContext.getBean(topicArray[0]);
// if (bean instanceof ITopicHandler) {
// ITopicHandler handler = (ITopicHandler)bean;
// try {
// /**
// * decode对象
// */
// Object param = handler.decode(message);
// /**
// * 处理
// */
// byte[] result = handler.handle(param);
// /**
// * 如果有返回值，并且topic的路径设计有返回写数据topic，就pub一下结果 比如 /test/t/1 /test 表示服务处理接收topic， /t/1 表示结果返回的topic
// */
// if (result != null && topicArray.length > 1) {
// applicationContext.getBean(RsocketClientSession.class).pub(getReturnTopic(topicArray), result);
// }
// } catch (Exception e) {
// log.warn("bean handling was error", e);
// }
// } else {
// log.warn("has no found topic handler by name={}", topic);
// }
// }
//
// private String getReturnTopic(String[] topicArray) {
// StringBuilder builder = new StringBuilder("/");
// for (int i = 1; i < topicArray.length; i++) {
// if (i == 1) {
// builder.append(topicArray[i]);
// } else {
// builder.append("/").append(topicArray[i]);
// }
// }
// return builder.toString();
// }
// }
