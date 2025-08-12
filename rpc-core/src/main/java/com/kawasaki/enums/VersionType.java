package com.kawasaki.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@ToString
@Getter
@AllArgsConstructor
public enum VersionType {
    VERSION1((byte) 1, "Version 1");
    private final byte code;
    private final String description;

    public static VersionType fromCode(byte code) {
        return Arrays.stream(values())
                .filter(o -> o.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown VersionType: " + code));
    }
}
