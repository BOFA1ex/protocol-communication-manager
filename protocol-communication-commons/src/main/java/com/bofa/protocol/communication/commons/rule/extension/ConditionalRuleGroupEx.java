package com.bofa.protocol.communication.commons.rule.extension;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.support.CompositeRule;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bofa1ex
 * @since 2020/3/28
 */
public class ConditionalRuleGroupEx extends CompositeRule {

    protected Set<Rule> successfulEvaluations;
    protected Rule conditionalRule;


    /**
     * Create a conditional rule group.
     *
     * @param name of the conditional rule
     */
    public ConditionalRuleGroupEx(String name) {
        super(name);
    }

    /**
     * Create a conditional rule group.
     *
     * @param name        of the conditional rule
     * @param description of the conditional rule
     */
    public ConditionalRuleGroupEx(String name, String description) {
        super(name, description);
    }

    /**
     * Create a conditional rule group.
     *
     * @param name        of the conditional rule
     * @param description of the conditional rule
     * @param priority    of the composite rule
     */
    public ConditionalRuleGroupEx(String name, String description, int priority) {
        super(name, description, priority);
    }

    /**
     * A path rule will trigger all it's rules if the path rule's condition is true.
     * @param facts The facts.
     * @return true if the path rules condition is true.
     */
    @Override
    public boolean evaluate(Facts facts) {
        successfulEvaluations = new HashSet<>();
        conditionalRule = getRuleWithHighestPriority();
        if (conditionalRule.evaluate(facts)) {
            for (Rule rule : rules) {
                if (rule != conditionalRule && rule.evaluate(facts)) {
                    successfulEvaluations.add(rule);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * When a conditional rule group is applied, all rules that evaluated to true are performed
     * in their natural order, but with the conditional rule (the one with the highest priority) first.
     *
     * @param facts The facts.
     *
     * @throws Exception thrown if an exception occurs during actions performing
     */
    @Override
    public void execute(Facts facts) throws Exception {
        conditionalRule.execute(facts);
        for (Rule rule : sort(successfulEvaluations)) {
            rule.execute(facts);
        }
    }

    private Rule getRuleWithHighestPriority() {
        List<Rule> copy = sort(rules);
        // make sure that we only have one rule with the highest priority
        Rule highest = copy.get(0);
        if (copy.size() > 1 && copy.get(1).getPriority() == highest.getPriority()) {
            throw new IllegalArgumentException("Only one rule can have highest priority");
        }
        return highest;
    }

    private List<Rule> sort(Set<Rule> rules) {
        // 源码中是升序排序, 我不太理解? priority越低优先级越高?
        return rules.parallelStream().sorted(Comparator.comparing(Rule::getPriority, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }
}
