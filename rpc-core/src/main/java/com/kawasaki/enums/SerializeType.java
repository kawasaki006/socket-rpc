package com.kawasaki.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Objects;

@ToString
@Getter
@AllArgsConstructor
public enum SerializeType {
    CUSTOM((byte) 0, "Custom"),
    KRYO((byte) 1, "Kryo");

    private final byte code;
    private final String description;

    public static SerializeType fromCode(byte code) {
        return Arrays.stream(values())
                .filter(o -> o.code == code)
                .findFirst()
                .orElse(CUSTOM);
    }

    public static SerializeType fromDesc(String description) {
        return Arrays.stream(values())
                .filter(o -> Objects.equals(o.description, description))
                .findFirst()
                .orElse(CUSTOM);
    }
}
