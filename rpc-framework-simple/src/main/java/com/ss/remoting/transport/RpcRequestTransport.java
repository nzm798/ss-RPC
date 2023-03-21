package com.ss.remoting.transport;

import com.ss.extension.SPI;
import com.ss.remoting.dto.RpcRequest;

/**
 * 传输RPC请求
 */
@SPI
public interface RpcRequestTransport {
    /**
     * send rpc request to server and get result
     * 向服务端传输请求并得到结果
     * @param rpcRequest 请求信息
     * @return 返回结果
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
