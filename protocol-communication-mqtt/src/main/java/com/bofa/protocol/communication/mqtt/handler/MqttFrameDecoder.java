package com.bofa.protocol.communication.mqtt.handler;

import com.bofa.protocol.codec.util.ByteBufUtils;
import com.bofa.protocol.communication.commons.netty.extension.ReplayingDecoderEx;
import com.bofa.protocol.communication.commons.rule.cmpt.RuleManager;
import com.bofa.protocol.communication.commons.rule.model.RulePacket;
import com.bofa.protocol.communication.mqtt.store.ClientSessionManager;
import io.netty.buffer.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static io.netty.handler.timeout.IdleState.READER_IDLE;

/**
 * @author bofa1ex
 * @since 2020/3/29
 */
public class MqttFrameDecoder extends ReplayingDecoderEx<Void> {

    static final Logger logger = LoggerFactory.getLogger(MqttFrameDecoder.class);

    @Autowired
    private RuleManager ruleManager;

    @Autowired
    private ClientSessionManager clientSessionManager;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            final String hex = ByteBufUtils.buffer2HexNonRead((ByteBuf) msg);
            logger.info("[Receive Data] {}", hex);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        //todo rulePacket对象可以考虑复用池, 减少内存开销, 在异步service执行完指令后显式clear
        ruleManager.fireRules(RulePacket.builder()
                .srcBuffer(in)
                .channel(ctx.channel())
                .build()
        );
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final String clientId = clientSessionManager.getClientId(ctx.channel());
        logger.info("[clientId: {}] 已断开通道！", clientId);
        // 清理channel的会话缓存
        clientSessionManager.unbind(ctx.channel());
        super.channelInactive(ctx);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == READER_IDLE) {
                final String clientId = clientSessionManager.getClientId(ctx.channel());
                // 客户端掉线
                logger.info("[{}] 通道长时间未通信 尝试断开！", clientId);
                ctx.close();
            }
        }
    }
}
