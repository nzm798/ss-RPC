package com.ss.exception;

/**
 * 处理序列化的异常
 */
public class SerializeException extends RuntimeException{
    public SerializeException(String message){
        super(message);
    }
}
