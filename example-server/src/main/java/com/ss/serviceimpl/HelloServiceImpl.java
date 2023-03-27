package com.ss.serviceimpl;

import com.ss.Hello;
import com.ss.HelloService;
import com.ss.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RpcService(version = "version1",group = "test1")
public class HelloServiceImpl implements HelloService {
    static {
        System.out.println("HelloServiceImpl被创建");
    }
    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl收到: {}.",hello.getMessage());
        String result="hello description is"+hello.getDescription();
        log.info("HelloServiceImpl返回: {}.",result);
        return result;
    }
}
