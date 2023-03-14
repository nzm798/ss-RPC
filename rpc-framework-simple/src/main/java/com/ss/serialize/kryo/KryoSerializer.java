package com.ss.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.ss.exception.SerializeException;
import com.ss.remoting.dto.RpcRequest;
import com.ss.remoting.dto.RpcResponse;
import com.ss.serialize.Serialize;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


@Slf4j
public class KryoSerializer implements Serialize {
    /**
     * 线程安全化
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);
        return kryo;
    });

    @Override
    public byte[] serialize(Object object) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) { //Output用来写入数据到字节数组缓冲区
            Kryo kryo = kryoThreadLocal.get();
            kryo.setRegistrationRequired(false); // 不需要注册类
            // Object->byte:将对象序列化为byte数组
            kryo.writeObject(output, object);
            kryoThreadLocal.remove(); //清空线程的Value，防止内存泄漏
            return output.toBytes(); //返回字符串组
        } catch (Exception e) {
            throw new SerializeException("Serialization failed");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            kryo.setRegistrationRequired(false);
            Object o = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return clazz.cast(o);//变成需要的返回值的类型
        } catch (Exception e) {
            throw new SerializeException("Deserialization failed");
        }
    }
}
