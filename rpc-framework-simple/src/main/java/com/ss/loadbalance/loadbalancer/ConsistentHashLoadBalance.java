package com.ss.loadbalance.loadbalancer;

import com.ss.loadbalance.AbstractLoadBalance;
import com.ss.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ConsistentHashLoadBalance extends AbstractLoadBalance {
    private final ConcurrentHashMap<String,ConsistentHashSelector> selectors=new ConcurrentHashMap<>();
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        return null;
    }
    static class ConsistentHashSelector{
        private final TreeMap<Long,String> virtualInvokers;
        private final int identityHashCode;
    }
}
