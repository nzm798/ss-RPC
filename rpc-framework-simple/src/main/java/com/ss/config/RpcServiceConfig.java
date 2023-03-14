package com.ss.config;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {
    /**
     * service version
     * 服务的版本
     */
    private String version = "";
    /**
     * when the interface has multiple implementation classes, distinguish by group
     * 当接口有多个实现类时，按组区分
     */
    private String group = "";

    /**
     * target service
     */
    private Object service;
    public String getServiceName(){
        return this.service.getClass().getInterfaces()[0].getCanonicalName(); //返回名称
    }
    public String getRpcServiceName(){
        return this.getServiceName()+this.getGroup()+this.getVersion();
    }
}
