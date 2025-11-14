package com.rnexchange.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.service.dto.QuoteDTO;
import com.rnexchange.service.marketdata.InstrumentState;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class QuoteMapperTest {

    private final QuoteMapper mapper = Mappers.getMapper(QuoteMapper.class);

    @Test
    @DisplayName("should map InstrumentState to QuoteDTO with derived fields")
    void shouldMapInstrumentStateToQuoteDto() {
        Clock clock = Clock.fixed(Instant.parse("2025-11-14T09:30:00Z"), ZoneOffset.UTC);

        InstrumentState state = new InstrumentState("RELIANCE", "NSE", new BigDecimal("100.00"), 0.01d, clock);

        state.updateWithTick(new BigDecimal("102.25"), 500L);

        QuoteDTO dto = mapper.toDto(state);

        assertThat(dto.symbol()).isEqualTo("RELIANCE");
        assertThat(dto.lastPrice()).isEqualByComparingTo("102.25");
        assertThat(dto.open()).isEqualByComparingTo("100.00");
        assertThat(dto.change()).isEqualByComparingTo("2.25");
        assertThat(dto.changePercent()).isEqualByComparingTo("2.25");
        assertThat(dto.volume()).isEqualTo(500L);
        assertThat(dto.timestamp()).isEqualTo(state.getLastUpdated());
    }
}
