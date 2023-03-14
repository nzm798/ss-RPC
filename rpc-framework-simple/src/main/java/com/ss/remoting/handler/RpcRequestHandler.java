package com.ss.remoting.handler;

import com.ss.exception.RpcException;
import com.ss.factory.SingletonFactory;
import com.ss.provider.ServiceProvider;
import com.ss.provider.impl.ZkServiceProviderImpl;
import com.ss.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * rpc request processor,处理器
 */
@Slf4j
public class RpcRequestHandler {
    private final ServiceProvider serviceProvider;
    public RpcRequestHandler(){
        serviceProvider= SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    /**
     * 处理RpcRequest请求：获得请求的方法并返回方法
     * @param rpcRequest 请求信息
     * @return 方法
     */
    public Object handler(RpcRequest rpcRequest){
        Object service=serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * get method execution results
     * 获得方法的执行结果
     *
     * @param rpcRequest client request
     * @param service    service object
     * @return the result of the target method execution
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest,Object service){
        Object result;
        try {
            Method method=service.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParamTypes());
            result=method.invoke(service,rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]",rpcRequest.getInterfaceName(),rpcRequest.getMethodName());
        }catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e){
            throw new RpcException(e.getMessage(),e);
        }
        return result;
    }
}
