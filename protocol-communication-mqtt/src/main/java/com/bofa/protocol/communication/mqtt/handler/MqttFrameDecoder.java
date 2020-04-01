package com.bofa.protocol.communication.mqtt.handler;

import com.bofa.protocol.codec.util.ByteBufUtils;
import com.bofa.protocol.communication.commons.rule.cmpt.RuleManager;
import com.bofa.protocol.communication.commons.rule.model.RulePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.*;

/**
 * @author bofa1ex
 * @since 2020/3/29
 */
public class MqttFrameDecoder extends ReplayingDecoder<Void> {

    static final Logger logger = LoggerFactory.getLogger(MqttFrameDecoder.class);

    @Autowired
    private RuleManager ruleManager;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            final String hex = ByteBufUtils.buffer2HexNonRead((ByteBuf) msg);
            logger.info("\n[Receive Data] {}", hex);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws InterruptedException, ExecutionException, TimeoutException {
        final RulePacket rulePacket = RulePacket.builder()
                .buffer(in)
                .channel(ctx.channel())
                .build();
        ruleManager.fireRules(rulePacket);
        out.add(rulePacket.getData().get(1000, TimeUnit.SECONDS));
        CompletableFuture.runAsync(rulePacket.getCallback());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("##################### 通道[{}]已连接！", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("##################### 通道[{}]已断开！", ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }
}
