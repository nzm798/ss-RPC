package com.ss.annotation;

import java.lang.annotation.*;

/**
 * rpc service的注释，用于标注在rpc 服务的实现类
 */
@Documented
@Retention(RetentionPolicy.RUNTIME) //注释保存到最后
@Target({ElementType.TYPE}) //说明注释作用的范围
@Inherited // 如果一个类用上了@Inherited修饰的注解，那么其子类也会继承这个注解
public @interface RpcService {
    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";
}
