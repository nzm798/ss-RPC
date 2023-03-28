package com.ss;

import com.ss.config.RpcServiceConfig;
import com.ss.proxy.RpcClientProxy;
import com.ss.remoting.transport.RpcRequestTransport;
import com.ss.remoting.transport.socket.SocketRpcClient;

public class SocketClientMain {
    public static void main(String[] args) {
        RpcRequestTransport requestTransport=new SocketRpcClient();
        RpcServiceConfig rpcServiceConfig=new RpcServiceConfig();
        RpcClientProxy rpcClientProxy=new RpcClientProxy(requestTransport,rpcServiceConfig);
        HelloService helloService=rpcClientProxy.getProxy(HelloService.class);
        String hello=helloService.hello(new Hello("fuck you"," just fuck you"));
        System.out.println(hello);
    }
}
