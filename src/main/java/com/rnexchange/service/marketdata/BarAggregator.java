package com.rnexchange.service.marketdata;

import com.rnexchange.service.dto.BarDTO;
import org.springframework.stereotype.Component;

@Component
public class BarAggregator {

    public BarDTO createBar(InstrumentState state) {
        return new BarDTO(
            state.getSymbol(),
            state.getSessionOpen(),
            state.getSessionHigh(),
            state.getSessionLow(),
            state.getLastPrice(),
            state.getCumulativeVolume(),
            state.getLastUpdatedMinuteBucket()
        );
    }
}
