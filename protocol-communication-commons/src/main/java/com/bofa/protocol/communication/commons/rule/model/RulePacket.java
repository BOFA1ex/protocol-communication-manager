package com.bofa.protocol.communication.commons.rule.model;

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
    private Channel channel;
    private Object data;
}
