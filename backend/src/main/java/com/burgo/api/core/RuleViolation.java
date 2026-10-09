package com.burgo.api.core;

/** A business rule was broken. The message is written for the customer and is safe to show. */
public class RuleViolation extends RuntimeException {
    public RuleViolation(String message) {
        super(message);
    }
}
