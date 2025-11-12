package com.rnexchange.service.mapper;

import com.rnexchange.domain.DailySettlementPrice;
import com.rnexchange.domain.Instrument;
import com.rnexchange.service.dto.DailySettlementPriceDTO;
import com.rnexchange.service.dto.InstrumentDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link DailySettlementPrice} and its DTO {@link DailySettlementPriceDTO}.
 */
@Mapper(componentModel = "spring")
public interface DailySettlementPriceMapper extends EntityMapper<DailySettlementPriceDTO, DailySettlementPrice> {
    @Mapping(target = "instrument", source = "instrument", qualifiedByName = "instrumentSymbol")
    DailySettlementPriceDTO toDto(DailySettlementPrice s);

    @Named("instrumentSymbol")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "symbol", source = "symbol")
    InstrumentDTO toDtoInstrumentSymbol(Instrument instrument);
}
