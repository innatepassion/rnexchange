package com.rnexchange.web.rest;

import com.rnexchange.security.AuthoritiesConstants;
import com.rnexchange.service.dto.BaselineSeedJobDTO;
import com.rnexchange.service.seed.BaselineSeedJobRegistry;
import com.rnexchange.service.seed.BaselineSeedService;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/baseline-seed")
public class BaselineSeedResource {

    private final BaselineSeedService baselineSeedService;
    private final BaselineSeedJobRegistry jobRegistry;

    public BaselineSeedResource(BaselineSeedService baselineSeedService, BaselineSeedJobRegistry jobRegistry) {
        this.baselineSeedService = baselineSeedService;
        this.jobRegistry = jobRegistry;
    }

    @PostMapping("/run")
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.EXCHANGE_OPERATOR + "')")
    public ResponseEntity<BaselineSeedJobDTO> runBaselineSeed(@RequestBody(required = false) BaselineSeedRunRequest requestBody) {
        BaselineSeedRunRequest body = Optional.ofNullable(requestBody).orElseGet(BaselineSeedRunRequest::new);
        UUID invocationId = Optional.ofNullable(body.getInvocationId()).orElse(UUID.randomUUID());

        BaselineSeedRequest.Builder builder = BaselineSeedRequest.builder()
            .invocationId(invocationId)
            .force(Boolean.TRUE.equals(body.getForce()));
        body.getContexts().forEach(builder::addContext);

        baselineSeedService.runBaselineSeedBlocking(builder.build());

        BaselineSeedJobDTO job = jobRegistry
            .findJob(invocationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Baseline seed job not found"));
        return ResponseEntity.accepted().body(job);
    }

    @GetMapping("/status/{jobId}")
    @PreAuthorize("hasAuthority('" + AuthoritiesConstants.EXCHANGE_OPERATOR + "')")
    public ResponseEntity<BaselineSeedJobDTO> getBaselineSeedStatus(@PathVariable UUID jobId) {
        return jobRegistry
            .findJob(jobId)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
    }

    public static class BaselineSeedRunRequest {

        private Boolean force;
        private Set<String> contexts = new LinkedHashSet<>();
        private UUID invocationId;

        public Boolean getForce() {
            return force;
        }

        public void setForce(Boolean force) {
            this.force = force;
        }

        public Set<String> getContexts() {
            return contexts;
        }

        public void setContexts(Set<String> contexts) {
            this.contexts = contexts != null ? new LinkedHashSet<>(contexts) : new LinkedHashSet<>();
        }

        public UUID getInvocationId() {
            return invocationId;
        }

        public void setInvocationId(UUID invocationId) {
            this.invocationId = invocationId;
        }
    }
}
