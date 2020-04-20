package com.bofa.protocol.communication.mqtt.service.rules;

import com.bofa.protocol.codec.mqtt.MqttParser;
import com.bofa.protocol.codec.mqtt.constants.MqttPacketTypeEnum;
import com.bofa.protocol.communication.commons.rule.cmpt.AbstractRule;
import com.bofa.protocol.communication.commons.rule.model.RulePacket;
import com.bofa.protocol.communication.mqtt.service.MqttDBService;
import org.jeasy.rules.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author bofa1ex
 * @since 2020/3/30
 */
@Rule(name = "mqttSubscribeRule", description = "mqtt subscribe 报文匹配规则")
public class MqttSubscribeRule extends AbstractRule {

    @Autowired
    private MqttParser mqttParser;

    @Autowired
    private MqttDBService mqttDBService;

    @Condition
    public boolean condition(@Fact(DEFAULT_FACTS_NAME) RulePacket packet) {
        return Optional.ofNullable(packet.getSrcBuffer()).map(buffer -> {
            final int packetType = buffer.getUnsignedByte(0) >> 4;
            return packetType == MqttPacketTypeEnum.SUBSCRIBE.packetType;
        }).orElse(Boolean.FALSE);
    }

    @Action
    public void action(@Fact(DEFAULT_FACTS_NAME) RulePacket packet) {
        packet.setData(mqttParser.decodeMqttSubscribePacket(packet.getSrcBuffer(), packet.getChannel()));
        CompletableFuture.runAsync(() -> mqttDBService.subscribe(packet), executor)
                .exceptionally(processError())
                .thenAccept(writeAndFlush(packet));
    }
}
