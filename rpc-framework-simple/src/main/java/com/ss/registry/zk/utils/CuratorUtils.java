package com.ss.registry.zk.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Curator(zookeeper client) utils
 * 用于连接和操作zookeeper
 */
@Slf4j
public final class CuratorUtils {
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    private CuratorUtils(){}

    /**
     * Create persistent nodes. Unlike temporary nodes, persistent nodes are not removed when the client disconnects
     * @param curatorFramework 用于操作zookeeper的节点
     * @param path 节点的地址
     */
    public static void createPersistentNode(CuratorFramework zkClient,String path){
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path)!=null){
                log.info("The node already exists. The node is:[{}]", path);
            }else {
                // 创建一个Model类型为永久节点，如果父节点不存在会对
                // eg: /my-rpc/github.javaguide.HelloService/127.0.0.1:9999
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("The node was created successfully. The node is:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        }catch (Exception e){
            log.error("create persistent node for path [{}] fail", path);
        }
    }

    /**
     * 获得节点下面的孩子节点
     * @param zkClient
     * @param rpcServiceName rpc服务的名称
     * @return 指定节点下面的所有孩子节点
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient,String rpcServiceName){
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)){
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result=null;
        String servicePath=ZK_REGISTER_ROOT_PATH+"/"+rpcServiceName;
        try {
            result=zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName,result);
            registerWatcher(rpcServiceName, zkClient);
        }catch (Exception e){
            log.error("get children nodes for path [{}] fail", servicePath);
        }
        return result;
    }

    /**
     * Registers to listen for changes to the specified node
     * 注册监听节点是否改变
     * @param rpcServiceName
     * @param zkClient
     * @throws Exception
     */
    private static void registerWatcher(String rpcServiceName,CuratorFramework zkClient) throws Exception{
        String servicePath=ZK_REGISTER_ROOT_PATH+"/"+rpcServiceName;
        PathChildrenCache pathChildrenCache=new PathChildrenCache(zkClient,servicePath,false);
        PathChildrenCacheListener pathChildrenCacheListener=(curatorFramework, pathChildrenCacheEvent)->{
            List<String> serviceAddress=curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName,serviceAddress);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }

    /**
     * 清空注册的节点
     * @param zkClient
     * @param inetSocketAddress
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress){
        REGISTERED_PATH_SET.stream().parallel().forEach(p->{
            try {
                if (p.endsWith(inetSocketAddress.toString())){
                    zkClient.delete().forPath(p);
                }
            }catch (Exception e){
                log.error("clear registry for path [{}] fail", p);
            }
        });
        log.info("All registered services on the server are cleared:[{}]", REGISTERED_PATH_SET.toString());
    }
}
