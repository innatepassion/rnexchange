package com.rnexchange.service.mapper;

import com.rnexchange.service.dto.ExchangeStatusDTO;
import com.rnexchange.service.dto.FeedStatusDTO;
import com.rnexchange.service.marketdata.ExchangeStatus;
import com.rnexchange.service.marketdata.FeedStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedStatusMapper {
    @Mapping(target = "globalState", source = "globalState")
    @Mapping(target = "startedAt", source = "startedAt")
    @Mapping(target = "exchanges", source = "exchanges")
    FeedStatusDTO toDto(FeedStatus status);

    @Mapping(target = "exchangeCode", source = "exchangeCode")
    @Mapping(target = "state", source = "state")
    @Mapping(target = "lastTickTime", source = "lastTickTime")
    @Mapping(target = "ticksPerSecond", source = "ticksPerSecond")
    @Mapping(target = "activeInstruments", source = "activeInstruments")
    ExchangeStatusDTO toDto(ExchangeStatus status);
}
