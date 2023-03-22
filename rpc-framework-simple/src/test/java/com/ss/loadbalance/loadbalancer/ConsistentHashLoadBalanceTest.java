package com.ss.loadbalance.loadbalancer;

import com.ss.DemoRpcService;
import com.ss.DemoRpcServiceImpl;
import com.ss.config.RpcServiceConfig;
import com.ss.enums.LoadBalanceEnum;
import com.ss.extension.ExtensionLoader;
import com.ss.loadbalance.LoadBalance;
import com.ss.remoting.dto.RpcRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ConsistentHashLoadBalanceTest {
    @Test
    void ConsistentHashLoadBalance(){
        LoadBalance loadBalance= ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(LoadBalanceEnum.LOADBALANCE.getName());
        List<String> serviceUrlList= new ArrayList<>(Arrays.asList("127.0.0.1:9997", "127.0.0.1:9998", "127.0.0.1:9999"));

        DemoRpcService demoRpcService=new DemoRpcServiceImpl();
        RpcServiceConfig rpcServiceConfig=RpcServiceConfig.builder()
                .service(demoRpcService)
                .version("version2")
                .group("test2")
                .build();
        RpcRequest rpcRequest=RpcRequest.builder()
                .parameters(demoRpcService.getClass().getTypeParameters())
                .interfaceName(rpcServiceConfig.getServiceName())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
        String userServiceAddress= loadBalance.selectServiceAddress(serviceUrlList,rpcRequest);
        assertEquals("127.0.0.1:9999", userServiceAddress);
    }
}
