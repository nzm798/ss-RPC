package com.ss.remoting.dto;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder //@Builder注释为你的类生成相对略微复杂的构建器API
@ToString
/**
 * 请求体
 */
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private String version;
    private String group;

    public String getRpcServiceName(){
        return this.getInterfaceName()+this.getGroup()+this.getVersion();
    }
}
