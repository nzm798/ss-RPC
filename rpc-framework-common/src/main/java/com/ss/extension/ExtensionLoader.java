package com.ss.extension;

import com.ss.extension.Holder;
import com.ss.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * refer to dubbo spi: https://dubbo.apache.org/zh-cn/docs/source_code_guide/dubbo-spi.html
 * 实例化扩展点
 */
@Slf4j
public final class ExtensionLoader<T> {
    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    private final Class<?> type;
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();
    private ExtensionLoader(Class<?> type){
        this.type=type;
    }
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type){
        if (type==null){
            throw new IllegalArgumentException("Extension type should not be null.");
        }
        if (!type.isInterface()){
            throw new IllegalArgumentException("Extension type must be an interface.");
        }
        if (type.getAnnotation(SPI.class)==null){
            throw new IllegalArgumentException("Extension type must be annotated by @SPI");
        }
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if (extensionLoader==null){
            EXTENSION_LOADERS.putIfAbsent(type,new ExtensionLoader<>(type));
            extensionLoader=(ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }
    public T getExtension(String name){
        if (StringUtil.isBlandk(name)){
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }
        Holder<Object> holder=cachedInstances.get(name);
        if (holder==null){
            cachedInstances.putIfAbsent(name,new Holder<>());
            holder=cachedInstances.get(name);
        }
        Object instance=holder.get();
        if (instance==null){
            synchronized (holder){
                instance=holder.get();
                if (instance==null){
                    instance=createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T)instance;
    }
    private T createExtension(String name){
        // load all extension classes of type T from file and get specific one by name
        // 从文件中加载所有的T类型的扩展类，并从中得到最为特殊的一个name
        Class<?> clazz = getExtensionClasses().get(name);
        T instance=(T) EXTENSION_INSTANCES.get(clazz);
        if (instance==null){
            try {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance=(T) EXTENSION_INSTANCES.get(name);
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }
        return instance;
    }
    private Map<String,Class<?>> getExtensionClasses(){
        //从缓存中加载扩展类
        Map<String, Class<?>> classes=cachedClasses.get();
        if (classes==null){
            synchronized (cachedClasses){
                classes=cachedClasses.get();
                if (classes==null){
                    classes=new HashMap<>();
                    // load all extensions from our extensions directory
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;

    }

    /**
     * 从文件中加载扩展类
     * @param extensionClasses 扩展类存储Map
     */
    private void loadDirectory(Map<String,Class<?>> extensionClasses){
        String filename=SERVICE_DIRECTORY+type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader=ExtensionLoader.class.getClassLoader();
            urls=classLoader.getResources(filename); //方便地获取打包在 JAR 文件中的资源文件的地址，而不用考虑资源文件实际存放在哪个目录或 JAR 文件中。
            if (urls!=null){
                while (urls.hasMoreElements()){
                    URL resourceUrl=urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }
    private void loadResource(Map<String ,Class<?>> extensionClasses,ClassLoader classLoader,URL resourceUrl){
        try(BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            while ((line=bufferedReader.readLine())!=null){
                // read every line
                final int ci=line.indexOf("#");
                if (ci>0){
                    line=line.substring(0,ci);
                }
                line=line.trim();
                try {
                    final int ei=line.indexOf("=");
                    String name=line.substring(0,ei).trim();
                    String ClazzName=line.substring(ei+1).trim();
                    // our SPI use key-value pair so both of them must not be empty
                    if (name.length()>0 && ClazzName.length()>0){
                        Class<?> Clazz=classLoader.loadClass(ClazzName);
                        extensionClasses.put(name,Clazz);
                    }
                }catch (ClassNotFoundException e){
                    log.error(e.getMessage());
                }
            }
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }
}
