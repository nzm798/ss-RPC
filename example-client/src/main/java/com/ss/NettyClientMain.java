package com.ss;

import com.ss.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@RpcScan(basePackage = "com.ss")
public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException{
        AnnotationConfigApplicationContext applicationContext=new AnnotationConfigApplicationContext(NettyClientMain.class);
        HelloController helloController=(HelloController) applicationContext.getBean("helloController");
        helloController.test();
    }
}
