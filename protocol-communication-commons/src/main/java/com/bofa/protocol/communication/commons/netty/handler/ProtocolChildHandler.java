package com.bofa.protocol.communication.commons.netty.handler;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author bofa1ex
 * @since 2020/3/29
 */
@Component
public abstract class ProtocolChildHandler extends ChannelInitializer<SocketChannel>  {

    static final Logger logger = LoggerFactory.getLogger(ProtocolChildHandler.class);

    @Value("${timeout}")
    private Integer timeout;

    @Value("${timeunit}")
    private String timeunit;

    /**
     * 处理粘包/拆包
     */
    @Lookup(value = "frameDecoder")
    abstract ByteToMessageDecoder frameDecoder();

    /**
     * 处理异步业务
     */
    @Autowired
    private SimpleChannelInboundHandler<?> businessHandler;

    protected void initChannel(SocketChannel channel) {
        channel.pipeline().addLast(new IdleStateHandler(timeout, 0,0, TimeUnit.valueOf(timeunit)))
                .addLast(frameDecoder())
                .addLast(businessHandler);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("##################### 通道已连接！");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.warn("##################### 通道已断开！");
    }
}
