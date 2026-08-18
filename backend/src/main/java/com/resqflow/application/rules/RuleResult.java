package com.resqflow.application.rules;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleResult {
    private boolean passed;
    private String reason;
    private String severity; // CRITICAL, WARNING, INFO
}
