package com.ss.annotation;

import com.ss.spring.CustomScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 扫描自定义的注释
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Import(CustomScannerRegistrar.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcScan {
    String[] basePackage();
}
