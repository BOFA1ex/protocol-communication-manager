package com.bofa.protocol.communication.mqtt.service.rules;

import com.bofa.protocol.codec.mqtt.MqttParser;
import com.bofa.protocol.codec.mqtt.outward.MqttConnectCommand;
import com.bofa.protocol.communication.commons.rule.cmpt.AbstractRule;
import com.bofa.protocol.communication.commons.rule.model.RulePacket;
import com.bofa.protocol.communication.mqtt.service.MqttDBService;
import com.google.common.base.Charsets;
import io.netty.buffer.Unpooled;
import org.jeasy.rules.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author bofa1ex
 * @since 2020/3/30
 */
@Rule(name = "mqttConnectRule", description = "mqtt connect 报文匹配规则")
public class MqttConnectRule extends AbstractRule {

    static final Logger logger = LoggerFactory.getLogger(MqttConnectRule.class);

    @Autowired
    private MqttParser mqttParser;

    @Autowired
    private MqttDBService dbService;

    @Condition
    public boolean isConnectPacketType(@Fact(DEFAULT_FACTS_NAME) RulePacket packet) {
        final Object command = packet.getData();
        return command instanceof MqttConnectCommand;
    }

    @Action
    public void action(@Fact(DEFAULT_FACTS_NAME) RulePacket packet) {
        // 组装connect ack包
//        MqttConnectAckCommand ackCommand = new MqttConnectAckCommand();
//        ackCommand.setxxx(123123);
//        ByteBuf buffer = mqttParser.encode(ackCommand, packet.getChannel());
        logger.info("hello world");
        dbService.authc();
        packet.getChannel().writeAndFlush(Unpooled.copiedBuffer("hello world", Charsets.UTF_8));
    }
}
