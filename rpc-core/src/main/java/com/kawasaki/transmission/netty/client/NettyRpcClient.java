package com.kawasaki.transmission.netty.client;

import cn.hutool.json.JSONUtil;
import com.kawasaki.constant.RpcConstant;
import com.kawasaki.dto.RpcMsg;
import com.kawasaki.dto.RpcReq;
import com.kawasaki.dto.RpcResp;
import com.kawasaki.enums.CompressType;
import com.kawasaki.enums.MsgType;
import com.kawasaki.enums.SerializeType;
import com.kawasaki.enums.VersionType;
import com.kawasaki.factory.SingletonFactory;
import com.kawasaki.registry.ServiceDiscovery;
import com.kawasaki.registry.impl.ZkServiceDiscovery;
import com.kawasaki.transmission.RpcClient;
import com.kawasaki.transmission.netty.codec.NettyRpcDecoder;
import com.kawasaki.transmission.netty.codec.NettyRpcEncoder;
import com.kawasaki.transmission.netty.server.NettyRpcServer;
import com.kawasaki.util.ConfigUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NettyRpcClient implements RpcClient {
    private static final Bootstrap bootstrap;
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private final ServiceDiscovery serviceDiscovery;
    private final ChannelPool channelPool;

    public NettyRpcClient() {
        this(SingletonFactory.getInstance(ZkServiceDiscovery.class));
    }

    public NettyRpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
        this.channelPool = SingletonFactory.getInstance(ChannelPool.class);
    }

    static {
        bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_CONNECT_TIMEOUT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        // trigger event if not sending anything to server in 5 secs
                        channel.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        channel.pipeline().addLast(new NettyRpcDecoder());
                        channel.pipeline().addLast(new NettyRpcEncoder());
                        channel.pipeline().addLast(new NettyRpcClientHandler());
                    }
                });
    }

    @SneakyThrows
    @Override
    public Future<RpcResp<?>> sendReq(RpcReq rpcReq) {
        CompletableFuture<RpcResp<?>> cf = new CompletableFuture<>();
        // save cf for handler later
        UnprocessedRpcReq.put(rpcReq.getReqId(), cf);
        // find a service address
        InetSocketAddress address = serviceDiscovery.findService(rpcReq);
        // blocking, until connected
        Channel channel = channelPool.get(address, () -> connect(address));
        log.info("Netty rpc client connected to: {}", address);

        String serializer = ConfigUtils.getRpcConfig().getSerializer();

        RpcMsg rpcMsg = RpcMsg.builder()
                .version(VersionType.VERSION1)
                .serializeType(SerializeType.fromDesc(serializer))
                .compressType(CompressType.GZIP)
                .msgType(MsgType.RPC_REQ)
                .data(rpcReq)
                .build();

        channel.writeAndFlush(rpcMsg).
                addListener((ChannelFutureListener) listener -> {
                    if (!listener.isSuccess()) {
                        listener.channel().close();
                        cf.completeExceptionally(listener.cause());
                    }
                });

        // return future to proxy that handles blocking
        return cf;
    }

    private Channel connect(InetSocketAddress address) {
        try {
            return bootstrap.connect(address).sync().channel();
        } catch (InterruptedException e) {
            log.error("Error connecting to netty server: ", e);
            throw new RuntimeException(e);
        }
    }
}
