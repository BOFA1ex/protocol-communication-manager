package com.bofa.protocol.communication.commons.rule.cmpt;

import com.bofa.protocol.communication.commons.rule.constant.RuleConstants;
import org.jeasy.rules.api.*;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author bofa1ex
 * @since 2020/3/30
 */
@Component
public class RuleManager implements RuleConstants {

    private final RulesEngine rulesEngine = new DefaultRulesEngine();

    private final Rules rules = new Rules();

    private final Facts facts = new Facts();

    public void register(Object rule){
        rules.register(rule);
    }

    @Async("asyncBusinessExecutor")
    public void fireRules(Object value){
        facts.put(DEFAULT_FACTS_NAME, value);
        rulesEngine.fire(rules, facts);
    }

    @Async("asyncBusinessExecutor")
    public Map<Rule, Boolean> checkRules(Facts facts){
        return rulesEngine.check(rules, facts);
    }
}
