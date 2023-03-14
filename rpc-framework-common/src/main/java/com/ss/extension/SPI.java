package com.ss.extension;

import java.lang.annotation.*;

@Documented //生成文档时会出现@SPI
@Retention(RetentionPolicy.RUNTIME) //表示这种注释会保存到那个阶段
@Target(ElementType.TYPE)
public @interface SPI {
}
