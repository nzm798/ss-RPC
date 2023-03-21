package com.ss.loadbalance.loadbalancer;

import com.ss.loadbalance.AbstractLoadBalance;
import com.ss.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * refer to dubbo consistent hash load balance
 */
@Slf4j
public class ConsistentHashLoadBalance extends AbstractLoadBalance {
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        int identityHashCode=System.identityHashCode(serviceAddresses);
        //创建rpc服务的名称
        String rpcServiceName=rpcRequest.getRpcServiceName();
        ConsistentHashSelector selector=selectors.get(rpcServiceName);
        if (selector==null || selector.identityHashCode!=identityHashCode){
            selectors.put(rpcServiceName,new ConsistentHashSelector(serviceAddresses,160,identityHashCode));
            selector=selectors.get(rpcServiceName);
        }
        return selector.select(rpcServiceName+ Arrays.stream(rpcRequest.getParameters()));
    }

    static class ConsistentHashSelector {
        private final TreeMap<Long, String> virtualInvokers;
        private final int identityHashCode;

        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;
            for (String invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    byte[] digest = md5(invoker + 1);
                    for (int h = 0; h < 4; h++) {
                        Long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        /**
         * 计算信息的MD5值
         *
         * @param key
         * @return
         */
        static byte[] md5(String key) {
            //信息摘要算法的功能
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            return md.digest();
        }

        /**
         * 计算hash值
         *
         * @param digest
         * @param idx
         * @return
         */
        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        public String select(String rpcServiceKey) {
            byte[] digest = md5(rpcServiceKey);
            return selectForKey(hash(digest, 0));
        }

        /**
         * 根据hashcode选择，选择key值大于hashcode值的数据
         * @param hashCode
         * @return
         */
        public String selectForKey(long hashCode) {
            //tailMap返回所有key值大于hashCode的集合
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();
            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }
            return entry.getValue();
        }
    }
}
