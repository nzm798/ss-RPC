package com.ss.registry.zk;

import com.ss.enums.LoadBalanceEnum;
import com.ss.enums.RpcErrorMessageEnum;
import com.ss.exception.RpcException;
import com.ss.extension.ExtensionLoader;

import com.ss.loadbalance.LoadBalance;
import com.ss.registry.ServiceDiscovery;
import com.ss.registry.zk.utils.CuratorUtils;
import com.ss.remoting.dto.RpcRequest;
import com.ss.utils.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 发现基于zookeeper的服务
 */
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadBalance;
    public ZkServiceDiscoveryImpl(){
        this.loadBalance= ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(LoadBalanceEnum.LOADBALANCE.getName());
    }
    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName=rpcRequest.getRpcServiceName();
        CuratorFramework zkClient= CuratorUtils.getZkClient();
        List<String> serviceUrlList=CuratorUtils.getChildrenNodes(zkClient,rpcServiceName);
        if (CollectionUtil.isEmpty(serviceUrlList)){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND,rpcServiceName);
        }
        // 负载均衡
        String targetServiceUrl=loadBalance.selectServiceAddress(serviceUrlList,rpcRequest);
        log.info("Successfully found the service address:[{}]",targetServiceUrl);
        String[] socketAddressArray=targetServiceUrl.split(":");
        String host=socketAddressArray[0];
        int port=Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host,port);
    }
}
