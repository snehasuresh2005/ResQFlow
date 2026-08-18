package com.resqflow.application.rules;

public interface Rule {
    RuleResult evaluate(RuleContext context);
}
