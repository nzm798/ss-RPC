package com.ss.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
/**
 * 枚举类用于RpcResponse类的返回值
 */
public enum RpcResponseCodeEnum {

    SUCCESS(200,"The remote call is successful"),
    FAIL(500,"The remote call is fail");
    private final int code;
    private final String message;
}
