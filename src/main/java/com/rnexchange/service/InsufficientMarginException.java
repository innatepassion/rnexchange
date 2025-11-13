package com.rnexchange.service;

import com.rnexchange.service.dto.MarginAssessment;

public class InsufficientMarginException extends RuntimeException {

    private final MarginAssessment assessment;

    public InsufficientMarginException(String message, MarginAssessment assessment) {
        super(message);
        this.assessment = assessment;
    }

    public MarginAssessment getAssessment() {
        return assessment;
    }
}
