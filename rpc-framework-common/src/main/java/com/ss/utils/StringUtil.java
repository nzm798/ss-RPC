package com.ss.utils;

/**
 * String 工具类
 */
public class StringUtil {
    /**
     * 判断是否为空白
     *
     * @param s
     * @return
     */
    public static boolean isBlandk(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {//判断是否为空白
                return false;
            }
        }
        return true;
    }
}
