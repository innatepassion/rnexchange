package com.rnexchange.service.mapper;

import com.rnexchange.service.dto.BarDTO;
import com.rnexchange.service.marketdata.InstrumentState;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BarMapper {
    @Mapping(target = "symbol", source = "symbol")
    @Mapping(target = "open", expression = "java(state.getSessionOpen())")
    @Mapping(target = "high", expression = "java(state.getSessionHigh())")
    @Mapping(target = "low", expression = "java(state.getSessionLow())")
    @Mapping(target = "close", expression = "java(state.getLastPrice())")
    @Mapping(target = "volume", expression = "java(state.getCumulativeVolume())")
    @Mapping(target = "timestamp", expression = "java(state.getLastUpdatedMinuteBucket())")
    BarDTO toDto(InstrumentState state);
}
