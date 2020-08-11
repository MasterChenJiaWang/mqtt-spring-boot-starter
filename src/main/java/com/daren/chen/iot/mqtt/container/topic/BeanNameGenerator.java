package com.daren.chen.iot.mqtt.container.topic;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

/**
 *
 */
public class BeanNameGenerator extends AnnotationBeanNameGenerator {

    @Override
    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
        // 从自定义注解中拿name
        String name = getNameByServiceFindAnnotation(definition, registry);
        if (name != null && !"".equals(name)) {
            return name;
        }
        // 走原来的方法
        return super.generateBeanName(definition, registry);
    }

    private String getNameByServiceFindAnnotation(BeanDefinition definition, BeanDefinitionRegistry registry) {
        String beanClassName = definition.getBeanClassName();
        try {
            Class<?> aClass = Class.forName(beanClassName);
            TopicHandler annotation = aClass.getAnnotation(TopicHandler.class);
            if (annotation == null) {
                return null;
            }
            return annotation.topic();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
