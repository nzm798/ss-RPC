package com.ss;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
//如果要进行序列化则必须实现该接口Serializable
public class Hello implements Serializable {
    private String message;
    private String description;
}
