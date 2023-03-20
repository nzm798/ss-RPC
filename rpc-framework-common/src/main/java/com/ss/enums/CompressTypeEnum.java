package com.ss.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 压缩方式
 */
@AllArgsConstructor
@Getter
public enum CompressTypeEnum {
    GZIP((byte) 0x01, "gzip"); // java对byte数组解压缩(zip,gzip,bzip2,jzlib)
    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (CompressTypeEnum c : CompressTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.getName();
            }
        }
        return null;
    }
}
