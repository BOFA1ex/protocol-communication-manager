package com.bofa.protocol.communication.mqtt.service;

import com.bofa.protocol.codec.mqtt.MqttParser;
import com.bofa.protocol.codec.mqtt.model.MqttConnectAckPacket;
import com.bofa.protocol.codec.mqtt.model.MqttConnectPacket;
import com.bofa.protocol.communication.commons.rule.model.RulePacket;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author bofa1ex
 * @since 2020/3/30
 */
@Async("asyncBusinessExecutor")
@Service
public class MqttDBService {

    static final Logger logger = LoggerFactory.getLogger(MqttDBService.class);

    @Autowired
    private MqttParser mqttParser;

    public void saveData(){
        //do nothing.
    }

    public void authc(RulePacket packet){
        logger.info("do authc");
        final MqttConnectAckPacket connectAckPacket = MqttConnectAckPacket.mapper((MqttConnectPacket) packet.getData(), MqttConnectAckPacket.ACK_SC, false);
        final ByteBuf buffer = mqttParser.encode(connectAckPacket, packet.getChannel());
        packet.getChannel().writeAndFlush(buffer);
    }
}

