package com.kawasaki.transmission.netty.codec;

import cn.hutool.core.compress.Gzip;
import com.kawasaki.compress.Compress;
import com.kawasaki.compress.impl.GzipCompress;
import com.kawasaki.constant.RpcConstant;
import com.kawasaki.dto.RpcMsg;
import com.kawasaki.factory.SingletonFactory;
import com.kawasaki.serialize.Serializer;
import com.kawasaki.serialize.impl.KryoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyRpcEncoder extends MessageToByteEncoder<RpcMsg> {
    private static final AtomicInteger ID_GEN = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMsg rpcMsg, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(RpcConstant.RPC_MAGIC_CODE);
        byteBuf.writeByte(rpcMsg.getVersion().getCode());

        // offset 4 bytes for data length
        byteBuf.writerIndex(byteBuf.writerIndex() + 4);

        byteBuf.writeByte(rpcMsg.getMsgType().getCode());
        byteBuf.writeByte(rpcMsg.getSerializeType().getCode());
        byteBuf.writeByte(rpcMsg.getCompressType().getCode());
        byteBuf.writeInt(ID_GEN.getAndIncrement());

        int msgLen = RpcConstant.REQ_HEAD_LEN;
        if (!rpcMsg.getMsgType().isHeartBeat()
            && !Objects.isNull(rpcMsg.getData())) {
            byte[] data = data2Bytes(rpcMsg);
            byteBuf.writeBytes(data);
            msgLen += data.length;
        }

        int curIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(curIndex - msgLen + RpcConstant.RPC_MAGIC_CODE.length + 1);
        byteBuf.writeInt(msgLen);
        byteBuf.writerIndex(curIndex);
    }

    private byte[] data2Bytes(RpcMsg rpcMsg) {
        Serializer serializer = SingletonFactory.getInstance(KryoSerializer.class);
        byte[] data = serializer.serialize(rpcMsg.getData());

        Compress compress = SingletonFactory.getInstance(GzipCompress.class);
        return compress.compress(data);
    }
}
