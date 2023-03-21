package com.ss.remoting.transport.socket;

import com.ss.enums.ServiceDiscoveryEnum;
import com.ss.exception.RpcException;
import com.ss.extension.ExtensionLoader;
import com.ss.registry.ServiceDiscovery;
import com.ss.remoting.dto.RpcRequest;
import com.ss.remoting.transport.RpcRequestTransport;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

@AllArgsConstructor
@Slf4j
public class SocketRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;
    public SocketRpcClient(){
        this.serviceDiscovery= ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(ServiceDiscoveryEnum.ZK.getName());
    }
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress=serviceDiscovery.lookupService(rpcRequest);
        try(Socket socket=new Socket()){
            socket.connect(inetSocketAddress);
            ObjectOutputStream outputStream=new ObjectOutputStream(socket.getOutputStream());
            //发送请求
            outputStream.writeObject(rpcRequest);
            ObjectInputStream inputStream=new ObjectInputStream(socket.getInputStream());
            //获得响应结果
            return inputStream.readObject();
        }catch (IOException | ClassNotFoundException e){
            throw new RpcException("调用服务失败：",e);
        }
    }

}
