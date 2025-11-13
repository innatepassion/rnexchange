package com.rnexchange.service.seed.dto;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class BaselineSeedRequest {

    private final boolean force;
    private final UUID invocationId;
    private final Set<String> contexts;

    private BaselineSeedRequest(boolean force, UUID invocationId, Set<String> contexts) {
        this.force = force;
        this.invocationId = invocationId;
        this.contexts = contexts;
    }

    public boolean isForce() {
        return force;
    }

    public UUID getInvocationId() {
        return invocationId;
    }

    public Set<String> getContexts() {
        return contexts;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private boolean force;
        private UUID invocationId;
        private final Set<String> contexts = new LinkedHashSet<>();

        private Builder() {}

        public Builder force(boolean force) {
            this.force = force;
            return this;
        }

        public Builder invocationId(UUID invocationId) {
            this.invocationId = invocationId;
            return this;
        }

        public Builder addContext(String context) {
            if (context != null) {
                contexts.add(context);
            }
            return this;
        }

        public Builder contexts(Set<String> contexts) {
            this.contexts.clear();
            if (contexts != null) {
                this.contexts.addAll(contexts);
            }
            return this;
        }

        public BaselineSeedRequest build() {
            UUID effectiveInvocationId = Objects.requireNonNullElseGet(invocationId, UUID::randomUUID);
            Set<String> effectiveContexts = new LinkedHashSet<>(contexts.isEmpty() ? Set.of("baseline") : contexts);
            return new BaselineSeedRequest(force, effectiveInvocationId, Collections.unmodifiableSet(effectiveContexts));
        }
    }
}
