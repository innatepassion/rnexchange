package com.rnexchange.service.mapper;

import com.rnexchange.service.dto.QuoteDTO;
import com.rnexchange.service.marketdata.InstrumentState;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuoteMapper {
    @Mapping(target = "change", expression = "java(state.getChange())")
    @Mapping(target = "changePercent", expression = "java(state.getChangePercent())")
    @Mapping(target = "open", expression = "java(state.getSessionOpen())")
    @Mapping(target = "volume", expression = "java(state.getCumulativeVolume())")
    @Mapping(target = "timestamp", expression = "java(state.getLastUpdated())")
    QuoteDTO toDto(InstrumentState state);
}
