package com.bofa.protocol.communication.mqtt;

import com.bofa.protocol.codec.util.ByteBufUtils;
import com.bofa.protocol.communication.commons.rule.extension.MVELRuleFactoryEx;
import com.bofa.protocol.communication.commons.rule.model.RulePacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jeasy.rules.api.*;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.springframework.core.io.ClassPathResource;

import java.util.concurrent.CompletableFuture;


/**
 * @author bofa1ex
 * @since 2020/3/28
 */
public class RuleAction {

    public static void main(String[] args) throws Exception {
        final ClassPathResource classPathResource = new ClassPathResource("rules");
        final Rules rules = new MVELRuleFactoryEx().scanDirectoryRules(classPathResource.getFile());
        final Facts entries = new Facts();
        final ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{0x01, 0x02, 0x03, 0x04});
        entries.put("model", RulePacket.builder().data(CompletableFuture.supplyAsync(() -> ByteBufUtils.buffer2HexNonRead(buffer))).build());
        RulesEngine rulesEngine = new DefaultRulesEngine();
        rulesEngine.fire(rules, entries);
    }
}
