package com.daren.chen.iot.mqtt.container.topic;

import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;

public class TopicHandlerFindScan implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
                                        BeanDefinitionRegistry beanDefinitionRegistry) {
        AnnotationAttributes annotationAttributes =
            AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(TopicHandlerScan.class.getName()));

        String[] basePackages = annotationAttributes.getStringArray("basePackage");
        // 没有设置 扫描路径,就扫描对应
        if (basePackages.length == 0) {
            basePackages = new String[] {
                ((StandardAnnotationMetadata)annotationMetadata).getIntrospectedClass().getPackage().getName()};
        }

        TopicHandlerRegisterService scanHandle = new TopicHandlerRegisterService(beanDefinitionRegistry, false);

        if (resourceLoader != null) {
            scanHandle.setResourceLoader(resourceLoader);
        }

        scanHandle.setBeanNameGenerator(new BeanNameGenerator());
        // 扫描指定路径下的接口
        Set<BeanDefinitionHolder> beanDefinitionHolders = scanHandle.doScan(basePackages);

    }

}
