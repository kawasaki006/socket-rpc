package com.kawasaki.transmission.netty.client;

import com.kawasaki.constant.RpcConstant;
import com.kawasaki.dto.RpcMsg;
import com.kawasaki.dto.RpcReq;
import com.kawasaki.dto.RpcResp;
import com.kawasaki.enums.CompressType;
import com.kawasaki.enums.MsgType;
import com.kawasaki.enums.SerializeType;
import com.kawasaki.enums.VersionType;
import com.kawasaki.util.ConfigUtils;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyRpcClientHandler extends SimpleChannelInboundHandler<RpcMsg> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMsg rpcMsg) throws Exception {
        if (rpcMsg.getMsgType().isHeartBeat()) {
            log.debug("Received heartbeat response from server");
            return;
        }

        log.debug("Received response from server: {}", rpcMsg);

        RpcResp<?> rpcResp = (RpcResp<?>) rpcMsg.getData();

        UnprocessedRpcReq.complete(rpcResp);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // if it's the 5 secs client idle event that's triggered
        boolean needHeartBeat = evt instanceof IdleStateEvent &&
                ((IdleStateEvent) evt).state() == IdleState.WRITER_IDLE;

        if (!needHeartBeat) {
            super.userEventTriggered(ctx, evt);
            return;
        }

        String serializer = ConfigUtils.getRpcConfig().getSerializer();

        // send heartbeat request message
        RpcMsg rpcMsg = RpcMsg.builder()
                .version(VersionType.VERSION1)
                .serializeType(SerializeType.fromDesc(serializer))
                .compressType(CompressType.GZIP)
                .msgType(MsgType.HEARTBEAT_REQ)
                .build();

        log.debug("Sending heartbeat request to server: {}", rpcMsg);
        ctx.writeAndFlush(rpcMsg)
                .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Server exception caught", cause);
        ctx.close();
    }
}
