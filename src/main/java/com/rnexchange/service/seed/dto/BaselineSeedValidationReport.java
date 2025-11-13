package com.rnexchange.service.seed.dto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class BaselineSeedValidationReport {

    private final boolean successful;
    private final List<String> errors;

    private BaselineSeedValidationReport(boolean successful, List<String> errors) {
        this.successful = successful;
        this.errors = errors;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public List<String> getErrors() {
        return errors;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private boolean successful;
        private List<String> errors = List.of();

        private Builder() {}

        public Builder successful(boolean successful) {
            this.successful = successful;
            return this;
        }

        public Builder errors(List<String> errors) {
            this.errors = Objects.requireNonNullElse(errors, List.of());
            return this;
        }

        public BaselineSeedValidationReport build() {
            return new BaselineSeedValidationReport(successful, Collections.unmodifiableList(errors));
        }
    }
}
