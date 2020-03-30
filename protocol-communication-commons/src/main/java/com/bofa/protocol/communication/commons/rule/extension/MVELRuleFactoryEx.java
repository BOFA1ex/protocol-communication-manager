package com.bofa.protocol.communication.commons.rule.extension;

import com.google.common.base.Strings;
import lombok.Builder;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.mvel.MVELRuleFactory;
import org.jeasy.rules.support.*;
import org.mvel2.ParserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * @author bofa1ex
 * @since 2020/3/28
 */
public class MVELRuleFactoryEx extends MVELRuleFactory {

    protected static final Logger logger = LoggerFactory.getLogger(MVELRuleFactoryEx.class);

    protected static final RuleDefinitionReader YML_READER = new YamlRuleDefinitionReader();
    protected static final RuleDefinitionReader JSON_READER = new JsonRuleDefinitionReader();
    protected static final ParserContext DEFAULT_PARSER_CONTEXT = new ParserContext();

    protected static final List<String> ALLOWED_COMPOSITE_RULE_TYPES = Arrays.asList(
            UnitRuleGroup.class.getSimpleName(),
            ConditionalRuleGroup.class.getSimpleName(),
            ActivationRuleGroup.class.getSimpleName()
    );

    protected RuleDefinitionReader reader;

    public MVELRuleFactoryEx() {
        super(YML_READER);
        this.reader = YML_READER;
    }

    /**
     * 新增了compositeRuleType策略
     */
    @Override
    protected Rule createCompositeRule(RuleDefinition ruleDefinition, ParserContext parserContext) {
        if (ruleDefinition.getCondition() != null) {
            logger.warn(
                    "Condition '{}' in composite rule '{}' of type {} will be ignored.",
                    ruleDefinition.getCondition(),
                    ruleDefinition.getName(),
                    ruleDefinition.getCompositeRuleType());
        }
        if (ruleDefinition.getActions() != null && !ruleDefinition.getActions().isEmpty()) {
            logger.warn(
                    "Actions '{}' in composite rule '{}' of type {} will be ignored.",
                    ruleDefinition.getActions(),
                    ruleDefinition.getName(),
                    ruleDefinition.getCompositeRuleType());
        }
        CompositeRule compositeRule;
        String name = ruleDefinition.getName();
        switch (ruleDefinition.getCompositeRuleType()) {
            case "UnitRuleGroup":
                compositeRule = new UnitRuleGroup(name);
                break;
            case "ActivationRuleGroup":
                compositeRule = new ActivationRuleGroup(name);
                break;
            case "ConditionalRuleGroup":
                compositeRule = new ConditionalRuleGroup(name);
                break;
            /* ****************** new strategy ******************/
            case "ConditionalRuleGroupEx":
                compositeRule = new ConditionalRuleGroupEx(name);
                break;
            default:
                throw new IllegalArgumentException("Invalid composite rule type, must be one of " + ALLOWED_COMPOSITE_RULE_TYPES);
        }
        compositeRule.setDescription(ruleDefinition.getDescription());
        compositeRule.setPriority(ruleDefinition.getPriority());

        for (RuleDefinition composingRuleDefinition : ruleDefinition.getComposingRules()) {
            compositeRule.addRule(createRule(composingRuleDefinition, parserContext));
        }

        return compositeRule;
    }

    /**
     * 提供扫描目录规则文件api
     * @param dir 目录
     * @return This class encapsulates a set of rules and represents a rules namespace.
     * Rules must have a unique name within a rules namespace.
     */
    public Rules scanDirectoryRules(File dir) throws IOException {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(Strings.lenientFormat("%s is not directory", dir.getName()));
        }
        final Rules rules = new Rules();
        Files.list(dir.toPath())
                .flatMap(path -> {
                    try {
                        return this.reader.read(Files.newBufferedReader(path)).stream();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(ruleDefinition -> super.createRule(ruleDefinition, DEFAULT_PARSER_CONTEXT))
                .forEach(rules::register);
        return rules;
    }
}
