package com.bofa.protocol.communication.commons.rule.model;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Builder;
import lombok.Data;

import java.util.concurrent.Future;

/**
 * @author bofa1ex
 * @since 2020/3/28
 */
@Data
@Builder
public class RulePacket {
    private ByteBuf buffer;
    private Channel channel;
    private Future<Object> data;
    private Runnable callback;
}
