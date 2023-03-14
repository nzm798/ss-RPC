package com.ss.remoting.transport.netty.server;

import com.ss.enums.CompressTypeEnum;
import com.ss.enums.RpcResponseCodeEnum;
import com.ss.enums.SerializationTypeEnum;
import com.ss.factory.SingletonFactory;
import com.ss.remoting.constants.RpcConstants;
import com.ss.remoting.dto.RpcMessage;
import com.ss.remoting.dto.RpcRequest;
import com.ss.remoting.dto.RpcResponse;
import com.ss.remoting.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * Customize the ChannelHandler of the server to process the data sent by the client.
 * 自定义服务器的通道处理程序以处理客户端发送的数据。
 * <p>
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 *
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {
    private final RpcRequestHandler rpcRequestHandler;
    public NettyRpcServerHandler(){
        rpcRequestHandler= SingletonFactory.getInstance(RpcRequestHandler.class);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg){
        try {
            if (msg instanceof RpcMessage){
                log.info("server receive msg: [{}] ", msg);
                byte messigeType=((RpcMessage)msg).getMessageType();
                RpcMessage rpcMessage=new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.HESSIAN.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                if (messigeType== RpcConstants.HEARTBEAT_REQUEST_TYPE){
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                }else {
                    RpcRequest rpcRequest=(RpcRequest) ((RpcMessage) msg).getData();
                    // Execute the target method (the method the client needs to execute) and return the method result
                    Object result=rpcRequestHandler.handler(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    if (ctx.channel().isActive() && ctx.channel().isWritable()){
                        RpcResponse<Object> rpcResponse=RpcResponse.success(result,rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    }else {
                        RpcResponse<Object> rpcResponse=RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.error("not writable now, message dropped");
                    }
                }
                //ch.writeAndFlush(message)也是异步的，该方法只是把发送消息加入了任务队列，这时直接关闭连接会导致问题。所以我们需要在消息发送完毕后在去关闭连接。
                //当消息传到后回调才会关闭
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 对应的触发事件下发送一个心跳包给服务端，检测服务端是否还存活，防止服务端已经宕机，客户端还不知道
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx,Object evt)throws Exception{
        if (evt instanceof IdleStateEvent){
            IdleState idleState=((IdleStateEvent)evt).state();
            // 长时间没有读操作时，长时间闲置关闭连接
            if (idleState==IdleState.READER_IDLE){
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        }else {
            super.userEventTriggered(ctx,evt);
        }
    }

    /**
     * Netty 的异常处理机制
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        log.error("server catch exception");
        cause.printStackTrace(); //在命令行打印异常信息在程序中出错的位置及原因
        ctx.close();
    }
}
