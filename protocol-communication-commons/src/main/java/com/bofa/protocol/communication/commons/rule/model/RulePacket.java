package com.bofa.protocol.communication.commons.rule.model;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Builder;
import lombok.Data;

/**
 * @author bofa1ex
 * @since 2020/3/28
 */
@Data
@Builder
public class RulePacket {
    private ByteBuf srcBuffer;
    private ByteBuf dstBuffer;
    private Channel channel;
    /* 标识符, 用于DB下发指令识别 */
    private String identifier;
    /* original data */
    private Object data;
}
