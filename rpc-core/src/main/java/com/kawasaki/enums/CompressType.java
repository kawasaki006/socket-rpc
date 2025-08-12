package com.kawasaki.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@ToString
@Getter
@AllArgsConstructor
public enum CompressType {
    GZIP((byte) 1, "Gzip");
    private final byte code;
    private final String description;

    public static CompressType fromCode(byte code) {
        return Arrays.stream(values())
                .filter(o -> o.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown CompressType: " + code));
    }
}
