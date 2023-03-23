package com.ss.remoting.transport.netty.client;

import com.ss.enums.CompressTypeEnum;
import com.ss.enums.SerializationTypeEnum;
import com.ss.enums.ServiceDiscoveryEnum;
import com.ss.extension.ExtensionLoader;
import com.ss.factory.SingletonFactory;
import com.ss.registry.ServiceDiscovery;
import com.ss.remoting.constants.RpcConstants;
import com.ss.remoting.dto.RpcMessage;
import com.ss.remoting.dto.RpcRequest;
import com.ss.remoting.dto.RpcResponse;
import com.ss.remoting.transport.RpcRequestTransport;
import com.ss.remoting.transport.netty.codec.RpcMessageDecoder;
import com.ss.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
@Slf4j
public final class NettyRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    public NettyRpcClient(){
        eventLoopGroup=new NioEventLoopGroup();
        bootstrap=new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //连接超时期限
                //如果时间超过或者不能建立连接，连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    //ChannelPipeline是一个handle的处理链对数据按下列顺序进行处理
                    @Override
                    protected void initChannel(SocketChannel ch){
                        ChannelPipeline p= ch.pipeline();
                        //如果在15秒内没有数据被送达服务端，则发送一个心跳包
                        p.addLast(new IdleStateHandler(0,5,0, TimeUnit.SECONDS));
                        p.addLast(new RpcMessageEncoder());
                        p.addLast(new RpcMessageDecoder());
                        p.addLast(new NettyRpcClientHandler());
                    }
                });
        this.serviceDiscovery= ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(ServiceDiscoveryEnum.ZK.getName());
        this.channelProvider= SingletonFactory.getInstance(ChannelProvider.class);
        this.unprocessedRequests=SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    /**
     * 建立连接并返回channel，这样就可以传送rpc 信息到服务端
     * @param inetSocketAddress
     * @return
     */
    @SneakyThrows //使其在编译的时候不报错。@SneakyThrows就是利用了这一机制，将当前方法抛出的异常，包装成RuntimeException，骗过编译器，使得调用点可以不用显示处理异常信息。
    public Channel doConnect(InetSocketAddress inetSocketAddress){
        CompletableFuture<Channel> completableFuture=new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future->{
            if (future.isSuccess()){
                log.info("The client has connected [{}] successful!",inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            }else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }
    public Channel getChannel(InetSocketAddress inetSocketAddress){
        Channel channel=channelProvider.get(inetSocketAddress);
        if (channel==null){
            channel=doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress,channel);
        }
        return channel;
    }
    public void close(){
        eventLoopGroup.shutdownGracefully();
    }
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        //建立返回的信息值
        CompletableFuture<RpcResponse<Object>> resultFuture=new CompletableFuture<>();
        //通过请求信息查找服务所在socket地址
        InetSocketAddress inetSocketAddress=serviceDiscovery.lookupService(rpcRequest);
        //通过inetSocketAddress获取相关channel
        Channel channel=getChannel(inetSocketAddress);
        if (channel.isActive()){
            unprocessedRequests.put(rpcRequest.getRequestId(),resultFuture);
            RpcMessage message=RpcMessage.builder()
                    .data(rpcRequest)
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .codec(SerializationTypeEnum.HESSIAN.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE)
                    .build();
            channel.writeAndFlush(message).addListener((ChannelFutureListener) future->{
                if (future.isSuccess()){
                    log.info("client send message: [{}]", message);
                }else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed",future.cause());
                }
            });
        }else {
            throw new IllegalStateException();
        }
        return resultFuture;

    }
}
