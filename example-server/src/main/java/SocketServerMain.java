import com.ss.HelloService;
import com.ss.config.RpcServiceConfig;
import com.ss.remoting.transport.socket.SocketRpcServer;
import com.ss.serviceimpl.HelloServiceImpl;

public class SocketServerMain {
    public static void main(String[] args) {
        HelloService helloService=new HelloServiceImpl();
        SocketRpcServer socketRpcServer=new SocketRpcServer();
        RpcServiceConfig rpcServiceConfig=new RpcServiceConfig();
        rpcServiceConfig.setService(helloService);
        socketRpcServer.registerService(rpcServiceConfig);
        socketRpcServer.start();
    }
}
