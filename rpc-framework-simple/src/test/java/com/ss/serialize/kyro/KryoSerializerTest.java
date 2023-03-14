package com.ss.serialize.kyro;

import com.ss.remoting.dto.RpcRequest;
import com.ss.serialize.kryo.KryoSerializer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

public class KryoSerializerTest {
    @Test
    void kryoSerializerTest(){
        RpcRequest target=RpcRequest.builder().methodName("hello")
                .parameters(new Object[]{"sayhelooloo", "sayhelooloosayhelooloo"})
                .interfaceName("com.ss.HelloService")
                .paramTypes(new Class<?>[]{String.class, String.class})
                .requestId(UUID.randomUUID().toString())
                .group("group1")
                .version("version1")
                .build();
        KryoSerializer kryoSerializer=new KryoSerializer();
        byte[] bytes= kryoSerializer.serialize(target);
        RpcRequest actual=kryoSerializer.deserialize(bytes,RpcRequest.class);
        assertEquals(target.getGroup(), actual.getGroup());
        assertEquals(target.getVersion(), actual.getVersion());
        assertEquals(target.getRequestId(), actual.getRequestId());

    }
}
