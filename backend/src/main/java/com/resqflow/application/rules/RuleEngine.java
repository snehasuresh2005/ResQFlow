package com.resqflow.application.rules;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RuleEngine {

    private final List<Rule> rules;

    public RuleEngine(List<Rule> rules) {
        this.rules = rules;
    }

    public List<RuleResult> evaluateAll(RuleContext context) {
        return rules.stream()
                .map(rule -> rule.evaluate(context))
                .collect(Collectors.toList());
    }

    public boolean checkAllPassed(RuleContext context) {
        return evaluateAll(context).stream().allMatch(RuleResult::isPassed);
    }
}
