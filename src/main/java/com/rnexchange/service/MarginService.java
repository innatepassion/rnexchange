package com.rnexchange.service;

import com.rnexchange.service.dto.MarginAssessment;
import com.rnexchange.service.dto.TraderOrderRequest;

public interface MarginService {
    MarginAssessment evaluateMargin(TraderOrderRequest request);
}
