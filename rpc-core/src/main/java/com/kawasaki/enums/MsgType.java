package com.kawasaki.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@ToString
@Getter
@AllArgsConstructor
public enum MsgType {
    HEARTBEAT_REQ((byte) 1, "Heartbeat request"),
    HEARTBEAT_RESP((byte) 2, "Heartbeat response"),
    RPC_REQ((byte) 3, "Rpc request"),
    RPC_RESP((byte) 4, "Rpc response");
    private final byte code;
    private final String description;

    public boolean isHeartBeat() {
        return this == HEARTBEAT_REQ || this == HEARTBEAT_RESP;
    }

    public boolean isReq() {
        return this == RPC_REQ || this == HEARTBEAT_REQ;
    }

    public static MsgType fromCode(byte code) {
        return Arrays.stream(values())
                .filter(o -> o.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown MsgType: " + code));
    }
}
