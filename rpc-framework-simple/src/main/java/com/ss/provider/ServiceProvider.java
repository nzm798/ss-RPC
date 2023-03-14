package com.ss.provider;

import com.ss.config.RpcServiceConfig;

/**
 * 储存和提供服务对象
 */
public interface ServiceProvider {
    /**
     * @param rpcServiceConfig rpc service related attributes
     *                         rpc服务相关属性
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * @param rpcServiceName rpc service name
     * @return service object
     */
    Object getService(String rpcServiceName);

    /**
     * @param rpcServiceConfig rpc service related attributes
     */
    void publishService(RpcServiceConfig rpcServiceConfig);
}
