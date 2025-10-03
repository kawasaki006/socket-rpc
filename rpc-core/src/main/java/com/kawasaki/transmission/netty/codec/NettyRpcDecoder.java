package com.kawasaki.transmission.netty.codec;

import cn.hutool.core.util.ArrayUtil;
import com.kawasaki.compress.Compress;
import com.kawasaki.compress.impl.GzipCompress;
import com.kawasaki.constant.RpcConstant;
import com.kawasaki.dto.RpcMsg;
import com.kawasaki.dto.RpcReq;
import com.kawasaki.dto.RpcResp;
import com.kawasaki.enums.CompressType;
import com.kawasaki.enums.MsgType;
import com.kawasaki.enums.SerializeType;
import com.kawasaki.enums.VersionType;
import com.kawasaki.exception.RpcException;
import com.kawasaki.factory.SingletonFactory;
import com.kawasaki.serialize.Serializer;
import com.kawasaki.serialize.impl.KryoSerializer;
import com.kawasaki.spi.CustomLoader;
import com.kawasaki.util.ConfigUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class NettyRpcDecoder extends LengthFieldBasedFrameDecoder {
    public NettyRpcDecoder() {
        super(RpcConstant.REQ_MAX_LEN, 5, 4, -9, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf buf = (ByteBuf) super.decode(ctx, in);

        return decodeFrame(buf);
    }

    private Object decodeFrame(ByteBuf buf) {
        // magic number
        readAndCheckMagicNumber(buf);

        // version type
        byte versionTypeCode = buf.readByte();
        VersionType versionType = VersionType.fromCode(versionTypeCode);

        // message length
        int msgLen = buf.readInt();

        // message type
        byte msgTypeCode = buf.readByte();
        MsgType msgType = MsgType.fromCode(msgTypeCode);

        // serializer type
        byte serializeTypeCode = buf.readByte();
        SerializeType serializeType = SerializeType.fromCode(serializeTypeCode);

        // compressor type
        byte compressTypeCode = buf.readByte();
        CompressType compressType = CompressType.fromCode(compressTypeCode);

        // req id
        int reqId = buf.readInt();

        // data
        Object data = readData(buf, msgLen - RpcConstant.REQ_HEAD_LEN, msgType,
                serializeType);

        return RpcMsg.builder()
                .reqId(reqId)
                .version(versionType)
                .msgType(msgType)
                .serializeType(serializeType)
                .compressType(compressType)
                .data(data)
                .build();
    }

    private void readAndCheckMagicNumber(ByteBuf buf) {
        byte[] magicBytes = new byte[RpcConstant.RPC_MAGIC_CODE.length];
        buf.readBytes(magicBytes);

        if (!ArrayUtil.equals(magicBytes, RpcConstant.RPC_MAGIC_CODE)) {
            throw new RpcException("Magic code error: " + new String(magicBytes));
        }
    }

    private Object readData(ByteBuf buf, int dataLen, MsgType msgType,
                            SerializeType serializeType) {
        if (msgType.isReq()) {
            // if msg is a request
            return readData(buf, dataLen, RpcReq.class, serializeType);
        } else {
            // if msg is a response
            return readData(buf, dataLen, RpcResp.class, serializeType);
        }
    }

    private <T> T readData(ByteBuf buf, int dataLen, Class<T> clazz,
                           SerializeType serializeType) {
        if (dataLen <= 0) {
            return null;
        }

        byte[] data = new byte[dataLen];
        buf.readBytes(data);

        Compress compress = SingletonFactory.getInstance(GzipCompress.class);
        data = compress.decompress(data);

        String serializerTypeStr = serializeType.getDescription();
        Serializer serializer = CustomLoader.getLoader(Serializer.class)
                .get(serializerTypeStr);

        return serializer.deserialize(data, clazz);
    }
}
