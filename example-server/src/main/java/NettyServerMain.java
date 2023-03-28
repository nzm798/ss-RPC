import com.ss.HelloService;
import com.ss.annotation.RpcScan;
import com.ss.config.RpcServiceConfig;
import com.ss.remoting.transport.netty.server.NettyRpcServer;
import com.ss.serviceimpl.HelloServiceImpl2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@RpcScan(basePackage = "com.ss")
public class NettyServerMain {
    public static void main(String[] args) {
        // AnnotationConfigApplicationContext 实例，用于加载和管理 Spring 容器中的 Bean。
        AnnotationConfigApplicationContext applicationContext=new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyRpcServer nettyRpcServer=(NettyRpcServer) applicationContext.getBean("nettyRpcServer");
        HelloService helloService2=new HelloServiceImpl2();
        RpcServiceConfig rpcServiceConfig=RpcServiceConfig.builder()
                .service(helloService2)
                .version("version2")
                .group("test2")
                .build();
        nettyRpcServer.registerService(rpcServiceConfig);
        nettyRpcServer.start();
    }
}
