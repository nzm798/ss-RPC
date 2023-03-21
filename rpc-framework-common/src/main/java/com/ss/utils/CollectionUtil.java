package com.ss.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * 判断集合是否为空的工具类
 */
public class CollectionUtil {
    public static boolean isEmpty(Collection<?> c){
        return c==null || c.isEmpty();
    }
}
