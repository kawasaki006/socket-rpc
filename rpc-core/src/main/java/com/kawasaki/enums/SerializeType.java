package com.kawasaki.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@ToString
@Getter
@AllArgsConstructor
public enum SerializeType {
    KRYO((byte) 1, "Kryo");
    private final byte code;
    private final String description;

    public static SerializeType fromCode(byte code) {
        return Arrays.stream(values())
                .filter(o -> o.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown SerializeType: " + code));
    }
}
