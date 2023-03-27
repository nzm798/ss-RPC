package com.ss.remoting.transport.netty.client;

import com.ss.enums.CompressTypeEnum;
import com.ss.enums.SerializationTypeEnum;
import com.ss.factory.SingletonFactory;
import com.ss.remoting.constants.RpcConstants;
import com.ss.remoting.dto.RpcMessage;
import com.ss.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

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

    /**
     * 读取服务端发来的信息
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg){
        try {
            log.info("client receive msg: [{}]", msg);
            if (msg instanceof RpcMessage){
                RpcMessage tmp=(RpcMessage) msg;
                byte msgType= tmp.getMessageType();
                if (msgType== RpcConstants.HEARTBEAT_RESPONSE_TYPE){
                    log.info("heart [{}]", tmp.getData());
                } else if (msgType==RpcConstants.RESPONSE_TYPE) {
                    RpcResponse<Object> rpcResponse=(RpcResponse<Object>) tmp.getData();
                    unprocessedRequests.complete(rpcResponse);
                }
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 实现固定触发的事件
     * @param ctx
     * @param evt IdleStateEvent类事件，表示连接的空闲状态事件，获得连接的状态
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx,Object evt)throws Exception{
        if (evt instanceof IdleState){
            IdleState state=((IdleStateEvent) evt).state();
            if (state==IdleState.WRITER_IDLE){
                log.info("write idle happen [{}]",ctx.channel().remoteAddress());
                Channel channel= nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage=new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PING);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE); //当连接失败时释放资源
            }
        }else {
            super.userEventTriggered(ctx,evt);
        }
    }

    /**
     * 当在客户端传输信息时出现异常时抛出
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
