package com.kawasaki.transmission.netty.server;

import com.kawasaki.config.RpcServiceConfig;
import com.kawasaki.constant.RpcConstant;
import com.kawasaki.factory.SingletonFactory;
import com.kawasaki.provider.ServiceProvider;
import com.kawasaki.provider.impl.ZkServiceProvider;
import com.kawasaki.transmission.RpcServer;
import com.kawasaki.transmission.netty.codec.NettyRpcDecoder;
import com.kawasaki.transmission.netty.codec.NettyRpcEncoder;
import com.kawasaki.util.ShutdownHookUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyRpcServer implements RpcServer {
    private final ServiceProvider serviceProvider;
    private final int port;

    public NettyRpcServer() {
        this(RpcConstant.SERVER_PORT);
    }

    public NettyRpcServer(int port) {
        this(SingletonFactory.getInstance(ZkServiceProvider.class), port);
    }

    public NettyRpcServer(ServiceProvider serviceProvider) {
        this(serviceProvider, RpcConstant.SERVER_PORT);
    }

    public NettyRpcServer(ServiceProvider serviceProvider, int port) {
        this.serviceProvider = serviceProvider;
        this.port = port;
    }

    @Override
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            // trigger event if not reading anything from client in 30 secs
                            channel.pipeline().addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            channel.pipeline().addLast(new NettyRpcDecoder());
                            channel.pipeline().addLast(new NettyRpcEncoder());
                            channel.pipeline().addLast(new NettyRpcServerHandler(serviceProvider));
                        }
                    });

            ShutdownHookUtils.clearAll();

            ChannelFuture future = bootstrap.bind(RpcConstant.SERVER_PORT).sync();
            log.info("Netty rpc server started on port: {}", RpcConstant.SERVER_PORT);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Server exception!");
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void publishService(RpcServiceConfig config) {
        serviceProvider.publishServices(config);
    }
}
