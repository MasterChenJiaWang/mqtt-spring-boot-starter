package com.daren.chen.iot.mqtt.container.topic;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
// spring中的注解,加载对应的类
@Import(TopicHandlerFindScan.class)
@Documented
public @interface TopicHandlerScan {
    /**
     *
     * @return
     */
    String[] basePackage() default {};
}
