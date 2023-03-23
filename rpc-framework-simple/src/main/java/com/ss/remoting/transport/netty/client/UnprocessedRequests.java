package com.ss.remoting.transport.netty.client;

import com.ss.remoting.dto.RpcResponse;
import io.netty.util.concurrent.CompleteFuture;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务未完成的请求
 */
public class UnprocessedRequests {
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES=new ConcurrentHashMap<>();
    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future){
        UNPROCESSED_RESPONSE_FUTURES.put(requestId,future);
    }
    public void complete(RpcResponse<Object> rpcResponse){
        CompletableFuture<RpcResponse<Object>> future=UNPROCESSED_RESPONSE_FUTURES.get(rpcResponse.getRequestId());
        if (null!=future){
            future.complete(rpcResponse);
        }else {
            throw new IllegalStateException(); //表示非法或不适当的时间调用了方法
        }
    }

}
