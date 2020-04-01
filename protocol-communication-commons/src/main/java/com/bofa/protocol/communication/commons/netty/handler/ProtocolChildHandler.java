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


    protected void initChannel(SocketChannel channel) {
        channel.pipeline().addLast(new IdleStateHandler(timeout, 0,0, TimeUnit.valueOf(timeunit)))
                .addLast(frameDecoder());
    }
}
