package com.bofa.protocol.communication.commons.netty;

import com.bofa.protocol.communication.commons.netty.handler.ProtocolChildHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author bofa1ex
 * @since 2020/3/29
 */
@Component
public class ProtocolServer {

    static final Logger logger = LoggerFactory.getLogger(ProtocolServer.class);

    @Value("${port}")
    private Integer port;

    @Value("${minimum}")
    private Integer minimum;

    @Value("${initial}")
    private Integer initial;

    @Value("${maximum}")
    private Integer maximum;

    @Autowired
    private ProtocolChildHandler childHandler;

    public void startServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(minimum, initial, maximum))
                    .childHandler(childHandler)
                    .option(ChannelOption.SO_BACKLOG, 1024);
            ChannelFuture chf = b.bind(port).sync();
            logger.info("##################### Netty服务端启动，监听端口 {}", port);
            chf.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("socket interrupted exception ", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
