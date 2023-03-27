package com.ss.proxy;

import com.ss.config.RpcServiceConfig;
import com.ss.enums.RpcErrorMessageEnum;
import com.ss.enums.RpcResponseCodeEnum;
import com.ss.exception.RpcException;
import com.ss.remoting.dto.RpcRequest;
import com.ss.remoting.dto.RpcResponse;
import com.ss.remoting.transport.RpcRequestTransport;
import com.ss.remoting.transport.netty.client.NettyRpcClient;
import com.ss.remoting.transport.socket.SocketRpcClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 动态代理类。
 * 当动态代理对象调用方法时，它实际上调用以下调用方法。
 * 正是因为动态代理，客户端调用的远程方法就像调用本地方法（中间进程被屏蔽）
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {
    private static final String INTERFACE_NAME="interfaceName";
    /**
     * Used to send requests to the server.And there are two implementations: socket and netty
     */
    private final RpcRequestTransport rpcRequestTransport;
    private final RpcServiceConfig rpcServiceConfig;
    public RpcClientProxy(RpcRequestTransport rpcRequestTransport,RpcServiceConfig rpcServiceConfig){
        this.rpcRequestTransport=rpcRequestTransport;
        this.rpcServiceConfig=rpcServiceConfig;
    }
    public RpcClientProxy(RpcRequestTransport rpcRequestTransport){
        this.rpcRequestTransport=rpcRequestTransport;
        this.rpcServiceConfig=new RpcServiceConfig();
    }

    /**
     * 返回动态代理实例
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},this);
    }

    /**
     * 当您使用代理对象调用方法时，实际上会调用此方法
     * 代理对象是你通过 getProxy 方法获取的对象。
     *
     * @param proxy the proxy instance that the method was invoked on
     *
     * @param method the {@code Method} instance corresponding to
     * the interface method invoked on the proxy instance.  The declaring
     * class of the {@code Method} object will be the interface that
     * the method was declared in, which may be a superinterface of the
     * proxy interface that the proxy class inherits the method through.
     *
     * @param args an array of objects containing the values of the
     * arguments passed in the method invocation on the proxy instance,
     * or {@code null} if interface method takes no arguments.
     * Arguments of primitive types are wrapped in instances of the
     * appropriate primitive wrapper class, such as
     * {@code java.lang.Integer} or {@code java.lang.Boolean}.
     *
     * @return
     * @throws Throwable
     */
    @Override
    @SneakyThrows
    public Object invoke(Object proxy, Method method, Object[] args){
        log.info("invoked method [{}]",method.getName());
        RpcRequest rpcRequest=RpcRequest.builder()
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .interfaceName(method.getDeclaringClass().getName())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .methodName(method.getName())
                .build();
        RpcResponse<Object> rpcResponse=null;
        if (rpcRequestTransport instanceof SocketRpcClient){
            rpcResponse=(RpcResponse<Object>) rpcRequestTransport.sendRpcRequest(rpcRequest);
        }
        if (rpcRequestTransport instanceof NettyRpcClient){
            CompletableFuture<RpcResponse<Object>> completableFuture=(CompletableFuture<RpcResponse<Object>>) rpcRequestTransport.sendRpcRequest(rpcRequest);
            rpcResponse=completableFuture.get();
        }
        this.check(rpcResponse,rpcRequest);
        return rpcResponse.getData();
    }
    private void check(RpcResponse<Object> rpcResponse,RpcRequest rpcRequest){
        if (rpcResponse==null){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE,INTERFACE_NAME+":"+rpcRequest.getInterfaceName());
        }
        if (!rpcResponse.getRequestId().equals(rpcRequest.getRequestId())){
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE,INTERFACE_NAME+":"+rpcRequest.getInterfaceName());
        }
        if (rpcResponse.getCode()==null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE,INTERFACE_NAME+":"+rpcRequest.getInterfaceName());
        }
    }
}
