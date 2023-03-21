package com.ss.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 负载均衡策略
 */
@AllArgsConstructor
@Getter
public enum LoadBalanceEnum {
    LOADBALANCE("loadBalance");
    private final String name;
}
