package com.ss.compress.gzip;

import com.ss.compress.Compress;
import com.ss.enums.CompressTypeEnum;
import com.ss.enums.SerializationTypeEnum;
import com.ss.extension.ExtensionLoader;
import com.ss.remoting.dto.RpcRequest;
import com.ss.serialize.Serialize;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GzipCompressTest {
    @Test
    void GzipCompressTest(){
        Compress compress= ExtensionLoader.getExtensionLoader(Compress.class).getExtension(CompressTypeEnum.GZIP.getName());
        RpcRequest rpcRequest = RpcRequest.builder().methodName("hello")
                .parameters(new Object[]{"sayhelooloo", "sayhelooloosayhelooloo"})
                .interfaceName("github.javaguide.HelloService")
                .paramTypes(new Class<?>[]{String.class, String.class})
                .requestId(UUID.randomUUID().toString())
                .group("group1")
                .version("version1")
                .build();
        Serialize kryoSerialize=ExtensionLoader.getExtensionLoader(Serialize.class).getExtension(SerializationTypeEnum.HESSIAN.getName());
        byte[] rpcRequestSerialize= kryoSerialize.serialize(rpcRequest);
        byte[] rpcRequestCompress=compress.compress(rpcRequestSerialize);
        byte[] resulr=compress.decompress(rpcRequestCompress);
        assertEquals(resulr.length,rpcRequestSerialize.length);
    }
}
