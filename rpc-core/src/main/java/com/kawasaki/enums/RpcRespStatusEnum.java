package com.kawasaki.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum RpcRespStatusEnum {
    SUCCESS(0, "success"),
    FAIL(9999, "fail");

    private final int code;
    private final String msg;
}
