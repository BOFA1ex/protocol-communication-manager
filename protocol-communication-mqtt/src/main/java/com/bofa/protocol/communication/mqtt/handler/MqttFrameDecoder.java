package com.bofa.protocol.communication.mqtt.handler;

import com.bofa.protocol.codec.mqtt.MqttParser;
import com.bofa.protocol.codec.mqtt.outward.MqttConnectCommand;
import com.bofa.protocol.codec.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author bofa1ex
 * @since 2020/3/29
 */
public class MqttFrameDecoder extends ReplayingDecoder<Void> {

    static final Logger logger = LoggerFactory.getLogger(MqttFrameDecoder.class);

    @Autowired
    private MqttParser mqttParser;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            final String data = ByteBufUtils.buffer2HexNonRead((ByteBuf) msg);
            logger.info("\n[Receive Data] {}", data);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        final MqttConnectCommand command = mqttParser.decode(in, ctx.channel());
        out.add(command);
    }
}
