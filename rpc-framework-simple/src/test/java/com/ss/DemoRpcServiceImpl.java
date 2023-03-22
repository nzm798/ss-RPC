package com.ss;

import com.ss.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;

/**
 * 测试的接口实现类
 */
@Slf4j
@RpcService(version = "version1",group = "test1")
public class DemoRpcServiceImpl implements DemoRpcService{
    @Override
    public String hello() {
        return "hello!";
    }
}
