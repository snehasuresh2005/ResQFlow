package com.resqflow.application.rules;

import com.resqflow.domain.resource.Resource;
import org.springframework.stereotype.Component;

@Component
public class ExpiryRule implements Rule {

    @Override
    public RuleResult evaluate(RuleContext context) {
        Resource resource = context.getResource();
        if (resource == null) {
            return RuleResult.builder().passed(false).reason("No resource provided").severity("CRITICAL").build();
        }

        if (resource.isExpired()) {
            return RuleResult.builder()
                    .passed(false)
                    .reason("Resource '" + resource.getName() + "' is expired (expiry: " + resource.getExpiryDate() + ")")
                    .severity("CRITICAL")
                    .build();
        }

        return RuleResult.builder().passed(true).reason("Resource is not expired").severity("INFO").build();
    }
}
