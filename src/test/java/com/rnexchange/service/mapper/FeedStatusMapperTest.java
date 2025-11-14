package com.rnexchange.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.service.dto.FeedState;
import com.rnexchange.service.dto.FeedStatusDTO;
import com.rnexchange.service.marketdata.ExchangeStatus;
import com.rnexchange.service.marketdata.FeedStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class FeedStatusMapperTest {

    private final FeedStatusMapper mapper = Mappers.getMapper(FeedStatusMapper.class);

    @Test
    @DisplayName("should map FeedStatus domain snapshot to FeedStatusDTO")
    void shouldMapFeedStatusToDto() {
        Clock clock = Clock.fixed(Instant.parse("2025-11-14T09:35:00Z"), ZoneOffset.UTC);
        Instant startedAt = clock.instant().minusSeconds(120);

        ExchangeStatus nseStatus = new ExchangeStatus("NSE", FeedState.RUNNING, clock.instant().minusSeconds(5), 42, 523);

        ExchangeStatus bseStatus = new ExchangeStatus("BSE", FeedState.HOLIDAY, clock.instant().minusSeconds(3600), 0, 0);

        FeedStatus snapshot = new FeedStatus(FeedState.RUNNING, startedAt, List.of(nseStatus, bseStatus));

        FeedStatusDTO dto = mapper.toDto(snapshot);

        assertThat(dto.globalState()).isEqualTo(FeedState.RUNNING);
        assertThat(dto.startedAt()).isEqualTo(startedAt);
        assertThat(dto.exchanges()).hasSize(2);

        assertThat(dto.exchanges().get(0).exchangeCode()).isEqualTo("NSE");
        assertThat(dto.exchanges().get(0).state()).isEqualTo(FeedState.RUNNING);
        assertThat(dto.exchanges().get(0).lastTickTime()).isEqualTo(nseStatus.lastTickTime());
        assertThat(dto.exchanges().get(0).ticksPerSecond()).isEqualTo(42);
        assertThat(dto.exchanges().get(0).activeInstruments()).isEqualTo(523);

        assertThat(dto.exchanges().get(1).exchangeCode()).isEqualTo("BSE");
        assertThat(dto.exchanges().get(1).state()).isEqualTo(FeedState.HOLIDAY);
        assertThat(dto.exchanges().get(1).lastTickTime()).isEqualTo(bseStatus.lastTickTime());
        assertThat(dto.exchanges().get(1).ticksPerSecond()).isEqualTo(0);
        assertThat(dto.exchanges().get(1).activeInstruments()).isEqualTo(0);
    }
}
