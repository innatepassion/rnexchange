package com.rnexchange.service.seed;

import java.util.UUID;

public class BaselineSeedJobInProgressException extends RuntimeException {

    public BaselineSeedJobInProgressException(UUID jobId) {
        super("Baseline seed job " + jobId + " is already running");
    }
}
