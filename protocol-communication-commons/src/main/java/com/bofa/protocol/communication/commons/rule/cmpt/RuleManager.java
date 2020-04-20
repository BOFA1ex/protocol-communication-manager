package com.bofa.protocol.communication.commons.rule.cmpt;

import com.bofa.protocol.communication.commons.rule.constant.RuleConstants;
import lombok.SneakyThrows;
import org.jeasy.rules.api.*;
import org.jeasy.rules.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author bofa1ex
 * @since 2020/3/30
 */
@Component
public class RuleManager implements RuleConstants {

    private final RulesEngine rulesEngine = new OnceExecuteRuleEngine();

    private final Rules rules = new Rules();

    private final Facts facts = new Facts();

    public void register(Object rule){
        rules.register(rule);
    }

    public void fireRules(Object value){
        facts.put(DEFAULT_FACTS_NAME, value);
        rulesEngine.fire(rules, facts);
    }

    public Map<Rule, Boolean> checkRules(Facts facts){
        return rulesEngine.check(rules, facts);
    }

    public static class OnceExecuteRuleEngine implements RulesEngine {

        private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRulesEngine.class);

        RulesEngineParameters parameters;
        List<RuleListener> ruleListeners;
        List<RulesEngineListener> rulesEngineListeners;

        protected OnceExecuteRuleEngine() {
            this(new RulesEngineParameters());
        }

        protected OnceExecuteRuleEngine(final RulesEngineParameters parameters) {
            this.parameters = parameters;
            this.ruleListeners = new ArrayList<>();
            this.rulesEngineListeners = new ArrayList<>();
        }

        /** 异常抛出去, 让ReplayingDecoder捕捉到 */
        @SneakyThrows
        @Override
        public void fire(Rules rules, Facts facts) {
            triggerListenersBeforeRules(rules, facts);
            doFire(rules, facts);
            triggerListenersAfterRules(rules, facts);
        }

        void doFire(Rules rules, Facts facts) throws Exception {
            if (rules.isEmpty()) {
                LOGGER.warn("No rules registered! Nothing to apply");
                return;
            }
            logEngineParameters();
            log(rules);
            log(facts);
            LOGGER.debug("Rules evaluation started");
            for (Rule rule : rules) {
                final String name = rule.getName();
                final int priority = rule.getPriority();
                if (priority > parameters.getPriorityThreshold()) {
                    LOGGER.debug("Rule priority threshold ({}) exceeded at rule '{}' with priority={}, next rules will be skipped",
                            parameters.getPriorityThreshold(), name, priority);
                    break;
                }
                if (!shouldBeEvaluated(rule, facts)) {
                    LOGGER.debug("Rule '{}' has been skipped before being evaluated",
                            name);
                    continue;
                }
                if (rule.evaluate(facts)) {
                    LOGGER.debug("Rule '{}' triggered", name);
                    triggerListenersAfterEvaluate(rule, facts, true);
                    try {
                        triggerListenersBeforeExecute(rule, facts);
                        rule.execute(facts);
                        LOGGER.debug("Rule '{}' performed successfully", name);
                        triggerListenersOnSuccess(rule, facts);
                        if (parameters.isSkipOnFirstAppliedRule()) {
                            LOGGER.debug("Next rules will be skipped since parameter skipOnFirstAppliedRule is set");
                            break;
                        }
                    } catch (Exception exception) {
                        LOGGER.error("Rule '" + name + "' performed with error", exception);
                        triggerListenersOnFailure(rule, exception, facts);
                        throw exception;
                    }
                    break;
                }
            }
        }

        private void logEngineParameters() {
            LOGGER.debug(parameters.toString());
        }

        private void log(Rules rules) {
            LOGGER.debug("Registered rules:");
            for (Rule rule : rules) {
                LOGGER.debug("Rule { name = '{}', description = '{}', priority = '{}'}",
                        rule.getName(), rule.getDescription(), rule.getPriority());
            }
        }

        private void log(Facts facts) {
            LOGGER.debug("Known facts:");
            for (Map.Entry<String, Object> fact : facts) {
                LOGGER.debug("Fact { {} : {} }",
                        fact.getKey(), fact.getValue());
            }
        }

        @Override
        public Map<Rule, Boolean> check(Rules rules, Facts facts) {
            triggerListenersBeforeRules(rules, facts);
            Map<Rule, Boolean> result = doCheck(rules, facts);
            triggerListenersAfterRules(rules, facts);
            return result;
        }

        private Map<Rule, Boolean> doCheck(Rules rules, Facts facts) {
            LOGGER.debug("Checking rules");
            Map<Rule, Boolean> result = new HashMap<>();
            for (Rule rule : rules) {
                if (shouldBeEvaluated(rule, facts)) {
                    result.put(rule, rule.evaluate(facts));
                }
            }
            return result;
        }

        private void triggerListenersOnFailure(final Rule rule, final Exception exception, Facts facts) {
            for (RuleListener ruleListener : ruleListeners) {
                ruleListener.onFailure(rule, facts, exception);
            }
        }

        private void triggerListenersOnSuccess(final Rule rule, Facts facts) {
            for (RuleListener ruleListener : ruleListeners) {
                ruleListener.onSuccess(rule, facts);
            }
        }

        private void triggerListenersBeforeExecute(final Rule rule, Facts facts) {
            for (RuleListener ruleListener : ruleListeners) {
                ruleListener.beforeExecute(rule, facts);
            }
        }

        private boolean triggerListenersBeforeEvaluate(Rule rule, Facts facts) {
            for (RuleListener ruleListener : ruleListeners) {
                if (!ruleListener.beforeEvaluate(rule, facts)) {
                    return false;
                }
            }
            return true;
        }

        private void triggerListenersAfterEvaluate(Rule rule, Facts facts, boolean evaluationResult) {
            for (RuleListener ruleListener : ruleListeners) {
                ruleListener.afterEvaluate(rule, facts, evaluationResult);
            }
        }

        private void triggerListenersBeforeRules(Rules rule, Facts facts) {
            for (RulesEngineListener rulesEngineListener : rulesEngineListeners) {
                rulesEngineListener.beforeEvaluate(rule, facts);
            }
        }

        private void triggerListenersAfterRules(Rules rule, Facts facts) {
            for (RulesEngineListener rulesEngineListener : rulesEngineListeners) {
                rulesEngineListener.afterExecute(rule, facts);
            }
        }

        private boolean shouldBeEvaluated(Rule rule, Facts facts) {
            return triggerListenersBeforeEvaluate(rule, facts);
        }

        @Override
        public RulesEngineParameters getParameters() {
            return new RulesEngineParameters(
                    parameters.isSkipOnFirstAppliedRule(),
                    parameters.isSkipOnFirstFailedRule(),
                    parameters.isSkipOnFirstNonTriggeredRule(),
                    parameters.getPriorityThreshold()
            );
        }

        @Override
        public List<RuleListener> getRuleListeners() {
            return Collections.unmodifiableList(ruleListeners);
        }

        @Override
        public List<RulesEngineListener> getRulesEngineListeners() {
            return Collections.unmodifiableList(rulesEngineListeners);
        }

        public void registerRuleListener(RuleListener ruleListener) {
            ruleListeners.add(ruleListener);
        }

        public void registerRuleListeners(List<RuleListener> ruleListeners) {
            this.ruleListeners.addAll(ruleListeners);
        }

        public void registerRulesEngineListener(RulesEngineListener rulesEngineListener) {
            rulesEngineListeners.add(rulesEngineListener);
        }

        public void registerRulesEngineListeners(List<RulesEngineListener> rulesEngineListeners) {
            this.rulesEngineListeners.addAll(rulesEngineListeners);
        }
    }
}
