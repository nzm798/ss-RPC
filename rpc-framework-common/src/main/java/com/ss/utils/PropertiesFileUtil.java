package com.ss.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 为了获得*.properties配置文件信息。
 */
@Slf4j
public final class PropertiesFileUtil {
    private PropertiesFileUtil() {

    }

    public static Properties readPropertiesFile(String fileName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("");//来得到当前的classpath的绝对路径的URI表示法。
        String rpcConfigPath="";
        if (url!=null){
            rpcConfigPath= url.getPath()+fileName;
        }
        Properties properties=null;
        try (InputStreamReader inputStreamReader=new InputStreamReader(new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)){
            properties=new Properties();
            properties.load(inputStreamReader);
        }catch (Exception e){
            log.error("occur exception when read properties file [{}]", fileName);
        }
        return properties;
    }
}
