package com.ss.registry.zk;

import com.ss.registry.ServiceRegistry;
import com.ss.registry.zk.utils.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

@Slf4j
public class ZkServiceRegistryImpl implements ServiceRegistry{
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath= CuratorUtils.ZK_REGISTER_ROOT_PATH+"/"+rpcServiceName+inetSocketAddress.toString();
        CuratorFramework zkClient=CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient,servicePath);
    }
}
