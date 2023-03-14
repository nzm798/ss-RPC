package com.ss.utils;

/**
 * 运行时工具类
 */
public class RuntimeUtil {
    /**
     *  获取cpu的核心数
     * @return 返回cpu的核心数
     */
    public static int cpus(){
        return Runtime.getRuntime().availableProcessors();
    }
}
