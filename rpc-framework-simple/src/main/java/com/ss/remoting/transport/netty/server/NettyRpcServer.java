package com.ss.remoting.transport.netty.server;

import com.ss.config.CustomShutdownHook;
import com.ss.config.RpcServiceConfig;
import com.ss.factory.SingletonFactory;
import com.ss.provider.ServiceProvider;
import com.ss.provider.impl.ZkServiceProviderImpl;
import com.ss.remoting.dto.RpcMessage;
import com.ss.remoting.transport.netty.codec.RpcMessageDecoder;
import com.ss.remoting.transport.netty.codec.RpcMessageEncoder;
import com.ss.utils.RuntimeUtil;
import com.ss.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * 客户端：
 * Server. Receive the client message, call the corresponding method according to the client message,
 * and then return the result to the client.
 * 接收信息，并调用相关方法返回信息结果给客户端
 */
@Slf4j
@Component
public class NettyRpcServer {
    public static final int PORT = 9998;
    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);

    /**
     * 注册服务
     *
     * @param rpcServiceConfig rpc服务信息
     */
    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    @SneakyThrows //生成抛出异常的代码
    public void start() {
        //添加关闭时的hook
        CustomShutdownHook.getCustomShutdownHook().cleanAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); //只是初始化并没有启动，接收信息处理severchannel
        EventLoopGroup workerGroup = new NioEventLoopGroup(); //客户端，处理channel
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(RuntimeUtil.cpus() * 2, ThreadPoolFactoryUtil.createThreadFactory("service-handle-group", false));
        try {
            ServerBootstrap b=new ServerBootstrap(); //用来创建通信的channel
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    // 是否开启 TCP 底层心跳机制,TCP在一段时间间隔后发送确定连接是否存在
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG,128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p=socketChannel.pipeline(); //ChannelPipeline负责编排ChannelHandle使其有序工作
                            // 30 秒之内没有收到客户端请求的话就关闭连接
                            p.addLast(new IdleStateHandler(30,0,0, TimeUnit.SECONDS));
                            p.addLast(new RpcMessageEncoder());
                            p.addLast(new RpcMessageDecoder());
                            p.addLast(serviceHandlerGroup,new NettyRpcServerHandler());
                        }
                    });
            // 绑定端口，同步等待绑定成功
            ChannelFuture f=b.bind(host,PORT).sync();// 进行异步
            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync(); // 判断如果端口连接不上就断开
        }catch (InterruptedException e){
            log.info("occur exception when start server:",e);
        }finally {
            log.error("shutdown bossGroup and workerGroup");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }
}
