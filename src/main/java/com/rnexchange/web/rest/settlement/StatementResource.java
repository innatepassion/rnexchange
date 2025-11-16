package com.rnexchange.web.rest.settlement;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class StatementResource {

    @GetMapping("/api/statements")
    @PreAuthorize("hasRole('TRADER')")
    public ResponseEntity<Void> listMyStatements() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/statements/{statementId}/html")
    @PreAuthorize("hasRole('TRADER')")
    public ResponseEntity<String> getStatementHtml(@PathVariable("statementId") UUID statementId) {
        return ResponseEntity.ok("");
    }
}
