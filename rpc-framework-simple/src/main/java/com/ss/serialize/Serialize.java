package com.ss.serialize;

import com.ss.extension.SPI;

/**
 * 序列化接口，所有序列化都需要实现接口
 */
@SPI
public interface Serialize{
    /**
     * 序列化
     * @param object 需要被序列化的对象
     * @return 返回byte[]序列化数据
     */
    byte[] serialize(Object object);

    /**
     * 反序列化
     * @param bytes 序列化后的字节数组
     * @param clazz 目标类
     * @return 反序列化的对象
     * @param <T> 类的类型。举个例子,  {@code String.class} 的类型是 {@code Class<String>}.
     *      *              如果不知道类的类型的话，使用 {@code Class<?>}
     */
    <T> T deserialize(byte[] bytes,Class<T> clazz);
}
