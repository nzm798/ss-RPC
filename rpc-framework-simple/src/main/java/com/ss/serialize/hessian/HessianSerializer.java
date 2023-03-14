package com.ss.serialize.hessian;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.ss.exception.SerializeException;
import com.ss.serialize.Serialize;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
public class HessianSerializer implements Serialize {
    @Override
    public byte[] serialize(Object object) {
        try (ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream()){
            HessianOutput hessianOutput=new HessianOutput(byteArrayOutputStream);
            hessianOutput.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        }catch (Exception e){
            throw new SerializeException("Serialization failed");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(bytes)){
            HessianInput hessianInput=new HessianInput(byteArrayInputStream);
            Object object=hessianInput.readObject();
            return clazz.cast(object);
        }catch (Exception e){
            throw new SerializeException("Deserialization failed");
        }
    }
}
