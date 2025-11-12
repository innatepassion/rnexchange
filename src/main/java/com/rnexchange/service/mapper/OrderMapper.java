package com.rnexchange.service.mapper;

import com.rnexchange.domain.Instrument;
import com.rnexchange.domain.Order;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.service.dto.InstrumentDTO;
import com.rnexchange.service.dto.OrderDTO;
import com.rnexchange.service.dto.TradingAccountDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Order} and its DTO {@link OrderDTO}.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper extends EntityMapper<OrderDTO, Order> {
    @Mapping(target = "tradingAccount", source = "tradingAccount", qualifiedByName = "tradingAccountId")
    @Mapping(target = "instrument", source = "instrument", qualifiedByName = "instrumentSymbol")
    OrderDTO toDto(Order s);

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
