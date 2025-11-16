package com.rnexchange.web.rest.settlement;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settlements")
public class SettlementResource {

    @PostMapping("/eod")
    @PreAuthorize("hasRole('EXCHANGE_OPERATOR')")
    public ResponseEntity<Void> runEod(@RequestParam("date") String date) {
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('EXCHANGE_OPERATOR')")
    public ResponseEntity<Void> list() {
        return ResponseEntity.ok().build();
    }
}
