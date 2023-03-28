package com.ss;

import com.ss.annotation.RpcReference;
import com.ss.annotation.RpcService;
import org.springframework.stereotype.Component;

@Component
public class HelloController {
    @RpcReference(version = "version1",group = "test1")
    private HelloService helloService;
    public void test()throws InterruptedException{
        String hello=this.helloService.hello(new Hello("111","222"));
        assert "Hello description is 222".equals(hello);
        Thread.sleep(12000);
        for (int i=0;i<10;i++){
            System.out.println(helloService.hello(new Hello("111", "222")));
        }
    }
}
