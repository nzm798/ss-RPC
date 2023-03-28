package com.ss.spring;

import com.ss.annotation.RpcScan;
import com.ss.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * 扫描和过滤指定的注释
 */
@Slf4j
public class CustomScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    private static final String SPRING_BEAN_BASE_PACKAGE="com.ss";
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME="basePackage";
    private ResourceLoader resourceLoader;
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader=resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry){
        //注解元数据（AnnotationMetadata）中获取 @RpcScan 注解的属性值，并生成一个对应的 AnnotationAttributes 实例
        //将获得的Map类型的属性值转换为AnnotationAttributes更加方便获得其中属性。
        AnnotationAttributes rpcScanAnnotationAttributes=AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(RpcScan.class.getName()));
        String[] rpcScanBasePackages=new String[0];
        if (rpcScanAnnotationAttributes!=null){
            //获得basePackage的属性值
            rpcScanBasePackages=rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        if (rpcScanBasePackages.length==0){
            rpcScanBasePackages=new String[]{((StandardAnnotationMetadata)annotationMetadata).getIntrospectedClass().getPackage().getName()};
        }
        //扫描rpcService注解
        CustomScanner rpcServiceScanner=new CustomScanner(beanDefinitionRegistry, RpcService.class);
        //扫描Component注解
        CustomScanner springBeanScanner=new CustomScanner(beanDefinitionRegistry, Component.class);
        if (resourceLoader!=null){
            //将资源加载器注入到扫描器中，使得在扫描时可以调用资源
            rpcServiceScanner.setResourceLoader(resourceLoader);
            springBeanScanner.setResourceLoader(resourceLoader);
        }
        int springBeanAmount= springBeanScanner.scan(SPRING_BEAN_BASE_PACKAGE);
        log.info("springBeanScanner扫描的数量为：[{}]",springBeanAmount);
        int rpcServiceCount= rpcServiceScanner.scan(rpcScanBasePackages);
        log.info("rpcServiceScanner扫描的数量 [{}]", rpcServiceCount);
    }
}
