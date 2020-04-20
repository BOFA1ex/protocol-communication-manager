package com.bofa.protocol.communication.mqtt.service;

import com.bofa.protocol.codec.mqtt.MqttParser;
import com.bofa.protocol.codec.mqtt.model.*;
import com.bofa.protocol.codec.util.ByteBufUtils;
import com.bofa.protocol.communication.commons.rule.model.RulePacket;
import com.bofa.protocol.communication.mqtt.store.ClientSessionManager;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author bofa1ex
 * @since 2020/3/30
 */
@Service
public class MqttDBService {

    static final Logger logger = LoggerFactory.getLogger(MqttDBService.class);

    @Resource(name = "businessExecutor")
    private ThreadPoolTaskExecutor executor;

    @Autowired
    private MqttParser mqttParser;

    @Autowired
    private ClientSessionManager clientSessionManager;

    public void authc(RulePacket packet) {
        final MqttConnectPacket data = (MqttConnectPacket) packet.getData();
        logger.info("[clientId: {}] 认证成功", data.getClientId());
        // 创建会话信息
        clientSessionManager.createClientSession(data, packet.getChannel());
        // 组装ConnectAck
        final MqttConnectAckPacket connectAckPacket = MqttConnectAckPacket.mapper(data, MqttConnectAckPacket.ACK_SC, false);
        ByteBuf buffer = mqttParser.encode(connectAckPacket, packet.getChannel());
        logger.info("[组装ConnectAck指令] {}", ByteBufUtils.buffer2HexNonRead(buffer));
        packet.setDstBuffer(buffer);
    }

    public void exit(RulePacket packet) {
        final String clientId = clientSessionManager.getClientId(packet.getChannel());
        logger.info("[{}] 注销成功", clientId);
        // 清理缓存, 并关闭通道
        packet.getChannel().close();
    }

    public void subscribe(RulePacket packet) {
        final MqttSubscribePacket data = (MqttSubscribePacket) packet.getData();
        final String clientId = clientSessionManager.getClientId(packet.getChannel());
        // 组装SubscribeAck
        final MqttSubscribeAckPacket mqttSubscribeAckPacket = MqttSubscribeAckPacket.mapper();
        mqttSubscribeAckPacket.setPacketIdentifier(data.getPacketIdentifier());
        final List<String> codes = data.getTopicFilters().stream()
                .peek(topicFilter -> logger.info("[clientId: {}] [topic: {}] [qos: {}] 订阅成功", clientId, topicFilter.getTopicFilter(), topicFilter.getRequestedQos()))
                // 加入透传缓存
                .peek(topicFilter -> clientSessionManager.bindTopic(topicFilter, packet.getChannel()))
                // 遍历topicFilters的服务质量级别要求
                .map(topicFilter -> Integer.toHexString(topicFilter.getRequestedQos()))
                .collect(Collectors.toList());
        mqttSubscribeAckPacket.setCode(codes);
        final ByteBuf buffer = mqttParser.encode(mqttSubscribeAckPacket, packet.getChannel());
        logger.info("[组装SubscribeAck指令] {}", ByteBufUtils.buffer2HexNonRead(buffer));
        packet.setDstBuffer(buffer);
    }

    public void publish(RulePacket packet) {
        final MqttPublishPacket data = (MqttPublishPacket) packet.getData();
        final String clientId = clientSessionManager.getClientId(packet.getChannel());
        logger.info("[clientId: {}] Publish [topic: {}] [qosLevel: {}] [payload: \"{}\"]", clientId, data.getTopicName(), data.getQosLevel(), data.getPayload());
        // 透传给订阅方
        final Integer qosLevel = data.getQosLevel();
        // 响应ack或者rec给publisher
        ByteBuf dstBuffer = null;
        if (qosLevel == 1) {
            final MqttPublishAckPacket mqttPublishAckPacket = MqttPublishAckPacket.mapper();
            mqttPublishAckPacket.setPacketIdentifier(data.getPacketIdentifier());
            dstBuffer = mqttParser.encode(mqttPublishAckPacket, packet.getChannel());
        } else if (qosLevel == 2) {
            final MqttPublishRecPacket mqttPublishRecPacket = MqttPublishRecPacket.mapper();
            mqttPublishRecPacket.setPacketIdentifier(data.getPacketIdentifier());
            dstBuffer = mqttParser.encode(mqttPublishRecPacket, packet.getChannel());
        }
        packet.setDstBuffer(dstBuffer);
        // 异步分发
        CompletableFuture.runAsync(() -> clientSessionManager.dispatch(data), executor);
    }

    public void pong(RulePacket packet) {
        final String clientId = clientSessionManager.getClientId(packet.getChannel());
        logger.info("[clientId: {}] 心跳包", clientId);
        final MqttPingResponsePacket mqttPingResponsePacket = MqttPingResponsePacket.mapper();
        final ByteBuf buffer = mqttParser.encode(mqttPingResponsePacket, packet.getChannel());
        logger.info("[组装PingResp指令] {}", ByteBufUtils.buffer2HexNonRead(buffer));
        packet.setDstBuffer(buffer);
    }

    public void publishAck(RulePacket packet) {
        final MqttPublishAckPacket data = (MqttPublishAckPacket) packet.getData();
        final String clientId = clientSessionManager.getClientId(packet.getChannel());
        logger.info("[clientId: {}] [packetId: {}] publish ack ", clientId, data.getPacketIdentifier());
        clientSessionManager.unbindPacketId(packet.getChannel(), data.getPacketIdentifier());
    }
}

