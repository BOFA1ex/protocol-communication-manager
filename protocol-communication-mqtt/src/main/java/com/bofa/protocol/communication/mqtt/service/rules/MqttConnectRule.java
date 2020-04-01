package com.bofa.protocol.communication.mqtt.service.rules;

import com.bofa.protocol.codec.mqtt.MqttParser;
import com.bofa.protocol.codec.mqtt.constants.MqttPacketTypeEnum;
import com.bofa.protocol.codec.mqtt.model.MqttConnectAckPacket;
import com.bofa.protocol.codec.mqtt.model.MqttConnectPacket;
import com.bofa.protocol.communication.commons.rule.cmpt.AbstractRule;
import com.bofa.protocol.communication.commons.rule.model.RulePacket;
import com.bofa.protocol.communication.mqtt.service.MqttDBService;
import io.netty.buffer.*;
import org.jeasy.rules.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author bofa1ex
 * @since 2020/3/30
 */
@Rule(name = "mqttConnectRule", description = "mqtt connect 报文匹配规则")
public class MqttConnectRule extends AbstractRule {

    @Autowired
    private MqttParser mqttParser;

    @Autowired
    private MqttDBService mqttDBService;

    @Condition
    public boolean isConnectPacketType(@Fact(DEFAULT_FACTS_NAME) RulePacket packet) {
        final ByteBuf buffer = packet.getBuffer();
        final int packetType = buffer.getUnsignedByte(0) >> 4;
        return packetType == MqttPacketTypeEnum.CONNECT.packetType;
    }

    @Action
    public void action(@Fact(DEFAULT_FACTS_NAME) RulePacket packet) {
        if (Objects.isNull(packet.getData())) {
            packet.setData(CompletableFuture.supplyAsync(
                    () -> mqttParser.decodeMqttConnectPacket(packet.getBuffer(), packet.getChannel()))
            );
        }
        packet.setCallback(() -> mqttDBService.authc(packet));
    }
}
