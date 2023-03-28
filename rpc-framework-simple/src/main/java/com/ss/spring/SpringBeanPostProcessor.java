package com.ss.spring;

import com.ss.annotation.RpcReference;
import com.ss.annotation.RpcService;
import com.ss.config.RpcServiceConfig;
import com.ss.enums.RpcRequestTransportEnum;
import com.ss.extension.ExtensionLoader;
import com.ss.factory.SingletonFactory;
import com.ss.provider.ServiceProvider;
import com.ss.provider.impl.ZkServiceProviderImpl;
import com.ss.proxy.RpcClientProxy;
import com.ss.remoting.transport.RpcRequestTransport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 在创建 Bean 之前调用此方法以查看类是否被注释,并添加额外操作
 */
@Component
@Slf4j
public class SpringBeanPostProcessor implements BeanPostProcessor {
    private final ServiceProvider serviceProvider;
    private final RpcRequestTransport rpcClient;
    public SpringBeanPostProcessor(){
        this.serviceProvider= SingletonFactory.getInstance(ZkServiceProviderImpl.class);
        this.rpcClient= ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension(RpcRequestTransportEnum.NETTY.getName());
    }

    /**
     * 在 Bean 对象的初始化方法被调用之前被调用，你可以利用这个方法来修改 Bean 对象的属性，从而增强 Bean 的对应行为。
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean,String beanName)throws BeansException{
        if (bean.getClass().isAnnotationPresent(RpcService.class)){
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            //获取指定 bean 对应的实现类上的 @RpcService 注解，并将其赋值给 rpcService 变量。
            RpcService rpcService=bean.getClass().getAnnotation(RpcService.class);
            RpcServiceConfig rpcServiceConfig=RpcServiceConfig.builder()
                    .version(rpcService.version())
                    .group(rpcService.group())
                    .service(bean)
                    .build();
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }

    /**
     * 在 Bean 对象的初始化方法被调用之之后被调用，你可以利用这个方法来修改 Bean 对象的属性，从而增强 Bean 的对应行为。
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    @SneakyThrows
    public Object postProcessAfterInitialization(Object bean,String beanName)throws BeansException{
        Class<?> targetClass=bean.getClass();
        //获取目标类中所有的成员变量，包括私有成员变量。
        Field[] declaredFields= targetClass.getDeclaredFields();
        for (Field declaredField:declaredFields){
            RpcReference rpcReference=declaredField.getAnnotation(RpcReference.class);
            if (rpcReference!=null){
                RpcServiceConfig rpcServiceConfig=RpcServiceConfig.builder()
                        .version(rpcReference.version())
                        .group(rpcReference.group())
                        .build();
                RpcClientProxy rpcClientProxy=new RpcClientProxy(rpcClient,rpcServiceConfig);
                //成员变量既是要远程调用的类或方法
                Object clientProxy=rpcClientProxy.getProxy(declaredField.getType());
                //打开目标类中的成员变量的访问控制权限，以便可以读取或修改其值。
                //如果要获取或修改 private、protected 或 default 权限的成员变量，需要在该成员变量上调用 setAccessible(true) 方法，将其访问权限设置为可访问，这样才能真正的获取或修改该成员变量的值。
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean,clientProxy);
                }catch (IllegalAccessException e){
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
