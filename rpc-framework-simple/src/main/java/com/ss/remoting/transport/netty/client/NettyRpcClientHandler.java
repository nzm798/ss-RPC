package com.ss.remoting.transport.netty.client;

import com.ss.factory.SingletonFactory;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * Customize the client ChannelHandler to process the data sent by the server
 * 自定义的客户端通道控制器，来处理服务端传来的数据
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;
    public NettyRpcClientHandler(){
        this.unprocessedRequests= SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient=SingletonFactory.getInstance(NettyRpcClient.class);
    }
}
