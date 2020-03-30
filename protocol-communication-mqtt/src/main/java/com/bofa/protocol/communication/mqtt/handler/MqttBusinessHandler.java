package com.bofa.protocol.communication.mqtt.handler;

import com.bofa.protocol.codec.mqtt.AbstractMqttCommand;
import com.bofa.protocol.communication.commons.rule.cmpt.RuleManager;
import com.bofa.protocol.communication.commons.rule.model.RulePacket;
import io.netty.channel.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author bofa1ex
 * @since 2020/3/30
 */
@ChannelHandler.Sharable
public class MqttBusinessHandler extends SimpleChannelInboundHandler<AbstractMqttCommand> {

    @Autowired
    private RuleManager ruleManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMqttCommand msg){
        ruleManager.fireRules(RulePacket.builder()
                .data(msg)
                .channel(ctx.channel())
                .build()
        );
    }
}
