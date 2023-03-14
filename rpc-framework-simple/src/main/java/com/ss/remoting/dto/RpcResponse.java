package com.ss.remoting.dto;

import com.ss.enums.RpcResponseCodeEnum;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
/**
 * 请求响应信息
 */
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID = 715745410605631233L;
    private String requestId;
    /**
     * 响应代码
     */
    private Integer code;
    /**
     * 响应信息
     */
    private String message;
    /**
     * 响应体
     */
    private T data;

    public static <T> RpcResponse<T> success(T data,String requestId){
        RpcResponse<T> response=new RpcResponse<>();
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        if (null != data){
            response.setData(data);
        }
        return response;
    }
    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum){
        RpcResponse<T> response=new RpcResponse<>();
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMessage());
        return response;
    }
}
