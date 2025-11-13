package com.rnexchange.service.seed;

import com.rnexchange.service.seed.dto.BaselineSeedRequest;

public interface BaselineSeedService {
    void runBaselineSeedBlocking(BaselineSeedRequest request);
}
