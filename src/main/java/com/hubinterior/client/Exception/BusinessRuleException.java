package com.hubinterior.client.Exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends ApiException {

    private final String ruleCode;

    public BusinessRuleException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, ErrorCode.BUSINESS_RULE_VIOLATION);
        this.ruleCode = null;
    }

    public BusinessRuleException(String message, String ruleCode) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, ErrorCode.BUSINESS_RULE_VIOLATION);
        this.ruleCode = ruleCode;
    }

    public String getRuleCode() {
        return ruleCode;
    }
}
