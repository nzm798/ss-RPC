package com.ss.registry;

import com.ss.DemoRpcService;
import com.ss.DemoRpcServiceImpl;
import com.ss.config.RpcServiceConfig;
import com.ss.enums.ServiceDiscoveryEnum;
import com.ss.enums.ServiceRegistryEnum;
import com.ss.extension.ExtensionLoader;
import com.ss.remoting.dto.RpcRequest;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZkServiceRegistryImplTest {
    @Test
    void should_register_service_successful_and_lookup_service_by_service_name(){
        ServiceRegistry zkServiceRegistry= ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(ServiceRegistryEnum.ZK.getName());
        InetSocketAddress givenInetSocketAddress=new InetSocketAddress("127.0.0.1",9333);
        DemoRpcService demoRpcService=new DemoRpcServiceImpl();
        RpcServiceConfig rpcServiceConfig= RpcServiceConfig.builder()
                .version("version2")
                .group("test2")
                .service(demoRpcService)
                .build();
        zkServiceRegistry.registerService(rpcServiceConfig.getRpcServiceName(),givenInetSocketAddress);
        ServiceDiscovery serviceDiscovery=ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(ServiceDiscoveryEnum.ZK.getName());
        RpcRequest rpcRequest = RpcRequest.builder()
//                .parameters(args)
                .interfaceName(rpcServiceConfig.getServiceName())
//                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
        InetSocketAddress findInetSocketAddress=serviceDiscovery.lookupService(rpcRequest);
        assertEquals(findInetSocketAddress,givenInetSocketAddress);
    }
}
