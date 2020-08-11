package com.daren.chen.iot.mqtt.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.daren.chen.iot.mqtt.api.RsocketMessageHandler;
import com.daren.chen.iot.mqtt.api.RsocketTopicManager;
import com.daren.chen.iot.mqtt.api.client.RsocketClientSession;
import com.daren.chen.iot.mqtt.api.server.RsocketServerSession;
import com.daren.chen.iot.mqtt.container.topic.TopicHandler;
import com.daren.chen.iot.mqtt.transport.client.TransportClient;
import com.daren.chen.iot.mqtt.transport.server.TransportServer;

@Configuration
// 开启自动配置，注册一个IdGeneratorProperties类型的配置bean到spring容器，同普通的@EnableAsync等开关一样
@EnableConfigurationProperties(IotConfig.class)
public class IotConfiguration implements ApplicationContextAware {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext)applicationContext;
    }

    @Bean
    @ConditionalOnProperty(prefix = "unittec.iot.mqtt.server", name = "enable", havingValue = "true")
    public RsocketServerSession initServer(@Autowired IotConfig iotConfig,
        @Autowired(required = false) AuthencationSession authencationSession,
        @Autowired(required = false) ExceptorAcceptor exceptorAcceptor,
        @Autowired(required = false) RsocketMessageHandler messageHandler,
        @Autowired(required = false) RsocketTopicManager rsocketTopicManager) {
        // 身份验证
        BiFunction<String, String,
            Boolean> auth = Optional.ofNullable(authencationSession)
                .map(au -> (BiFunction<String, String, Boolean>)authencationSession::auth)
                .orElse((userName, password) -> true);
        // 异常处理
        Consumer<Throwable> throwableConsumer =
            Optional.ofNullable(exceptorAcceptor).map(ea -> (Consumer<Throwable>)ea::accept).orElse(ts -> {
            });
        return TransportServer.create(iotConfig.getServer().getHost(), iotConfig.getServer().getPort())
            .heart(iotConfig.getServer().getHeart()).log(iotConfig.getServer().isLog())
            .protocol(iotConfig.getServer().getProtocol()).revBufSize(iotConfig.getServer().getRevBufSize())
            .sendBufSize(iotConfig.getServer().getSendBufSize()).noDelay(iotConfig.getServer().isNoDelay())
            .keepAlive(iotConfig.getServer().isKeepAlive()).auth(auth).ssl(iotConfig.getServer().isSsl())
            .messageHandler(messageHandler).topicManager(rsocketTopicManager).exception(throwableConsumer).start()
            .block();
    }

    @Bean
    // 配置信息 unittec.iot.mqtt.client.enable =true 生效
    @ConditionalOnProperty(prefix = "unittec.iot.mqtt.client", name = "enable", havingValue = "true")
    public RsocketClientSession initClient(@Autowired IotConfig iotConfig) {
        IotConfig.Client client = iotConfig.getClient();
        MessageAcceptor messageAcceptor = this.applicationContext.getBean(MessageAcceptor.class);
        ExceptorAcceptor exceptorAcceptor = this.applicationContext.getBean(ExceptorAcceptor.class);
        OnCloseListener onCloseListener = this.applicationContext.getBean(OnCloseListener.class);

        String[] names = this.applicationContext.getBeanFactory().getBeanNamesForAnnotation(TopicHandler.class);
        if (names.length > 0) {
            if (client.getSubTopics() == null) {
                client.setSubTopics(new ArrayList<>());
            }
            client.getSubTopics().addAll(Arrays.asList(names));
        }
        return TransportClient.create(client.getIp(), client.getPort()).heart(client.getHeart())
            .subTopics(iotConfig.getClient().getSubTopics()).protocol(client.getProtocol()).ssl(client.isSsl())
            .log(client.isLog()).clientId(client.getOption().getClientIdentifier())
            .password(client.getOption().getPassword()).username(client.getOption().getUserName())
            .willMessage(client.getOption().getWillMessage()).willTopic(client.getOption().getWillTopic())
            .willQos(client.getOption().getWillQos())
            .onClose(() -> Optional.of(onCloseListener).ifPresent(OnCloseListener::start))
            .exception(throwable -> Optional.of(exceptorAcceptor).ifPresent(ec -> ec.accept(throwable)))
            .messageAcceptor(messageAcceptor::accept).connect().block();
    }

}
