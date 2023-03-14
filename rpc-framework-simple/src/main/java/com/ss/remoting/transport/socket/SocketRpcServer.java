package com.ss.remoting.transport.socket;


import com.ss.config.RpcServiceConfig;
import com.ss.factory.SingletonFactory;
import com.ss.provider.ServiceProvider;
import com.ss.provider.impl.ZkServiceProviderImpl;
import com.ss.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
@Slf4j
public class SocketRpcServer {
    private final ExecutorService threadPool;
    private final ServiceProvider serviceProvider;
    public SocketRpcServer(){
        threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    /**
     * 注册服务
     * @param rpcServiceConfig 服务信息类
     */
    public void registerService(RpcServiceConfig rpcServiceConfig){

    }

    /**
     * 启动服务
     */
    public void start(){

    }

}
