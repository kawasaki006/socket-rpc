package com.kawasaki.dto;

import com.kawasaki.enums.CompressType;
import com.kawasaki.enums.MsgType;
import com.kawasaki.enums.SerializeType;
import com.kawasaki.enums.VersionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpcMsg implements Serializable {
    private static final long serialVersionUID = 1;

    private Integer reqId;
    private VersionType version;
    private MsgType msgType;
    private SerializeType serializeType;
    private CompressType compressType;
    private Object data;

}
