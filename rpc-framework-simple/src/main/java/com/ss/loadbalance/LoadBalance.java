package com.ss.loadbalance;

import com.ss.extension.SPI;
import com.ss.remoting.dto.RpcRequest;

import java.util.List;

/**
 * 负载平衡接口
 */
@SPI
public interface LoadBalance {
    /**
     * Choose one from the list of existing service addresses list
     * 从列表中选择一个存在的服务地址
     * @param serviceUrlList 服务地址列表
     * @param rpcRequest rpc请求
     * @return 目标服务地址
     */
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
