package com.ss.remoting.transport.socket;

import com.ss.config.CustomShutdownHook;
import com.ss.config.RpcServiceConfig;
import com.ss.factory.SingletonFactory;
import com.ss.provider.ServiceProvider;
import com.ss.provider.impl.ZkServiceProviderImpl;
import com.ss.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import static com.ss.remoting.transport.netty.server.NettyRpcServer.PORT;

@Slf4j
public class SocketRpcServer {
    private final ExecutorService threadPool;
    private final ServiceProvider serviceProvider;
    public SocketRpcServer(){
        threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    /**
     * 注册服务
     * @param rpcServiceConfig 服务信息类
     */
    public void registerService(RpcServiceConfig rpcServiceConfig){
        serviceProvider.publishService(rpcServiceConfig);
    }

    /**
     * 启动服务
     */
    public void start(){
        try (ServerSocket serverSocket=new ServerSocket()){
            String host= InetAddress.getLocalHost().getHostAddress();
            serverSocket.bind(new InetSocketAddress(host,PORT));
            CustomShutdownHook.getCustomShutdownHook().cleanAll();
            Socket socket;
            while ((socket=serverSocket.accept())!=null){
                log.info("client connected [{}]", socket.getInetAddress());
                //加入线程接受和回复请求
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        }catch (IOException e){
            log.error("occur IoException",e);
        }
    }

}
