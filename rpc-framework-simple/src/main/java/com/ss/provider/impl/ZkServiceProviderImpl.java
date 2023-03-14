package com.ss.provider.impl;

import com.ss.config.RpcServiceConfig;
import com.ss.enums.RpcErrorMessageEnum;
import com.ss.enums.ServiceRegistryEnum;
import com.ss.exception.RpcException;
import com.ss.extension.ExtensionLoader;
import com.ss.provider.ServiceProvider;
import com.ss.registry.ServiceRegistry;
import com.ss.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务提供者实现类
 */
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {
    /**
     * key: rpc service name(interface name + version + group)
     * value: service object
     */
    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(ServiceRegistryEnum.ZK.getName());
    }

    /**
     * 添加注册服务的信息
     * @param rpcServiceConfig rpc service related attributes
     *                         rpc服务相关属性
     */
    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcserviceName=rpcServiceConfig.getRpcServiceName();
        if (serviceMap.containsKey(rpcserviceName)){
            return;
        }
        registeredService.add(rpcserviceName);
        serviceMap.put(rpcserviceName,rpcServiceConfig.getService());
        log.info("Add service: {} and interfaces:{}",rpcserviceName,rpcServiceConfig.getService().getClass().getInterfaces());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service=serviceMap.get(rpcServiceName);
        if (null == service){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    /**
     * 注册服务
     * @param rpcServiceConfig rpc service related attributes
     */
    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host= InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig); //调用之前的添加信息的方法
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), new InetSocketAddress(host, NettyRpcServer.PORT));
        }catch (UnknownHostException e){
            log.error("occur exception when getHostAddress", e);
        }
    }
}
