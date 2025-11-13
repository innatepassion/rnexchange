package com.rnexchange.service.seed;

import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import com.rnexchange.service.seed.dto.BaselineSeedValidationReport;

public interface BaselinePrerequisiteValidator {
    BaselineSeedValidationReport validate(BaselineSeedRequest request);
}
