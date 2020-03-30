package com.bofa.protocol.communication.commons.rule.cmpt;


import com.bofa.protocol.communication.commons.rule.constant.RuleConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author bofa1ex
 * @since 2020/3/30
 */
public abstract class AbstractRule implements InitializingBean, RuleConstants {

    @Autowired
    private RuleManager ruleManager;

    @Override
    public /* 子类无需复写 */ final void afterPropertiesSet(){
        ruleManager.register(this);
    }
}
