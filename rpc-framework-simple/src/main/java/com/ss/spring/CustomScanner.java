package com.ss.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

/**
 * 自定义的包扫描器
 */
public class CustomScanner extends ClassPathBeanDefinitionScanner {
    public CustomScanner(BeanDefinitionRegistry registry, Class<? extends Annotation> annoType) {
        super(registry);
        //添加过滤器，指定注解的类型
        super.addIncludeFilter(new AnnotationTypeFilter(annoType));
    }
    @Override
    public int scan(String... basePackages){
        return super.scan(basePackages);
    }
}
