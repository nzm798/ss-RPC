package com.ss.registry;

import com.ss.extension.SPI;
import com.ss.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 服务发现
 */
@SPI
public interface ServiceDiscovery {
    /**
     * 通过rpcServiceName寻找服务
     * @param rpcRequest rpcRequest pojo
     * @return 服务地址
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
