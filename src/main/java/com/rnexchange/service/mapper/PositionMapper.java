package com.rnexchange.service.mapper;

import com.rnexchange.domain.Instrument;
import com.rnexchange.domain.Position;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.service.dto.InstrumentDTO;
import com.rnexchange.service.dto.PositionDTO;
import com.rnexchange.service.dto.TradingAccountDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Position} and its DTO {@link PositionDTO}.
 */
@Mapper(componentModel = "spring")
public interface PositionMapper extends EntityMapper<PositionDTO, Position> {
    @Mapping(target = "tradingAccount", source = "tradingAccount", qualifiedByName = "tradingAccountId")
    @Mapping(target = "instrument", source = "instrument", qualifiedByName = "instrumentSymbol")
    PositionDTO toDto(Position s);

    @Named("tradingAccountId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    TradingAccountDTO toDtoTradingAccountId(TradingAccount tradingAccount);

    @Named("instrumentSymbol")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "symbol", source = "symbol")
    InstrumentDTO toDtoInstrumentSymbol(Instrument instrument);
}
