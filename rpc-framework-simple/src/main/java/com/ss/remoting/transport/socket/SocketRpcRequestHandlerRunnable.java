package com.ss.remoting.transport.socket;

import com.ss.factory.SingletonFactory;
import com.ss.remoting.dto.RpcRequest;
import com.ss.remoting.dto.RpcResponse;
import com.ss.remoting.handler.RpcRequestHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Slf4j
public class SocketRpcRequestHandlerRunnable implements Runnable{
    private final Socket socket;
    private final RpcRequestHandler rpcRequestHandler;
    public SocketRpcRequestHandlerRunnable(Socket socket){
        this.socket=socket;
        this.rpcRequestHandler= SingletonFactory.getInstance(RpcRequestHandler.class);
    }
    @Override
    public void run() {
        log.info("server handle message from client by thread: [{}]",Thread.currentThread().getName());
        try (ObjectInputStream objectInputStream=new ObjectInputStream(socket.getInputStream())
             ; ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream())){
            RpcRequest rpcRequest=(RpcRequest) objectInputStream.readObject();
            Object result=rpcRequestHandler.handler(rpcRequest);
            objectOutputStream.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));
            objectOutputStream.flush();
        }catch (IOException | ClassNotFoundException e){
            log.error("occur exception",e);
        }
    }
}
