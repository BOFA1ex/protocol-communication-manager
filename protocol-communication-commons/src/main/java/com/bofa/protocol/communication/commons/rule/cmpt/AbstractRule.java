package com.bofa.protocol.communication.commons.rule.cmpt;


import com.bofa.protocol.communication.commons.rule.constant.RuleConstants;
import com.bofa.protocol.communication.commons.rule.model.RulePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.function.*;

/**
 * @author bofa1ex
 * @since 2020/3/30
 */
public abstract class AbstractRule implements InitializingBean, RuleConstants {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractRule.class);

    @Autowired
    private RuleManager ruleManager;

    @Resource(name = "businessExecutor")
    protected ThreadPoolTaskExecutor executor;

    @Override
    public /* 子类无需复写 */ final void afterPropertiesSet() {
        ruleManager.register(this);
    }

    protected Function<Throwable, ? extends Void> processError(){
        return t -> {
            logger.error("解析失败: {}",t.getMessage());
            return null;
        };
    }

    protected Consumer<Void> writeAndFlush(RulePacket packet) {
        return v -> {
            // 下发dst指令(判空处理)
            Optional.ofNullable(packet.getDstBuffer()).ifPresent(buffer -> packet.getChannel().writeAndFlush(buffer));
        };
    }
}
