package com.ss;

import com.ss.annotation.RpcScan;
import com.ss.remoting.transport.netty.client.NettyRpcClient;
import com.ss.remoting.transport.netty.server.NettyRpcServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@RpcScan(basePackage = "com.ss")
public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException{
        AnnotationConfigApplicationContext applicationContext=new AnnotationConfigApplicationContext(NettyClientMain.class);
        HelloController helloController=(HelloController) applicationContext.getBean("helloController");
        helloController.test();
    }
}
