package com.kawasaki.transmission.netty.server;

import com.kawasaki.dto.RpcMsg;
import com.kawasaki.dto.RpcReq;
import com.kawasaki.dto.RpcResp;
import com.kawasaki.enums.CompressType;
import com.kawasaki.enums.MsgType;
import com.kawasaki.enums.SerializeType;
import com.kawasaki.enums.VersionType;
import com.kawasaki.factory.SingletonFactory;
import com.kawasaki.handler.RpcReqHandler;
import com.kawasaki.provider.ServiceProvider;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyRpcServerHandler extends SimpleChannelInboundHandler<RpcMsg> {
    private final RpcReqHandler rpcReqHandler;

    public NettyRpcServerHandler(ServiceProvider serviceProvider) {
        this.rpcReqHandler = new RpcReqHandler(serviceProvider);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMsg rpcMsg) throws Exception {
        log.debug("Received data from client: {}", rpcMsg);

        MsgType msgType;
        Object data;
        if (rpcMsg.getMsgType().isHeartBeat()) {
            msgType = MsgType.HEARTBEAT_RESP;
            data = null;
        } else {
            msgType = MsgType.RPC_RESP;
            RpcReq rpcReq = (RpcReq) rpcMsg.getData();
            // process rpc request (get service impl from provider and invoke it)
            data = handleRpcReq(rpcReq);
        }

        RpcMsg msg = RpcMsg.builder()
                .reqId(rpcMsg.getReqId())
                .version(VersionType.VERSION1)
                .msgType(msgType)
                .compressType(CompressType.GZIP)
                .serializeType(SerializeType.KRYO)
                .data(data)
                .build();

        ctx.channel()
                .writeAndFlush(msg)
                .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        boolean needClose = evt instanceof IdleStateEvent
                && ((IdleStateEvent) evt).state() == IdleState.READER_IDLE;

        if (!needClose) {
            super.userEventTriggered(ctx, evt);
            return;
        }

        log.debug("Server did not receive heartbeat for 30 secs, close channel, address: {}",
                ctx.channel().remoteAddress());
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Server error caught: ", cause);
        ctx.close();
    }

    private RpcResp<?> handleRpcReq(RpcReq rpcReq) {
        try {
            Object o = rpcReqHandler.invoke(rpcReq);
            return RpcResp.success(rpcReq.getReqId(), o);
        } catch (Exception e) {
            log.error("failed to invoke rpc request method", e);

            return RpcResp.fail(rpcReq.getReqId(), e.getMessage());
        }
    }
}
