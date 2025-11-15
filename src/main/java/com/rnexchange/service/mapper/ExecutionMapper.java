package com.rnexchange.service.mapper;

import com.rnexchange.domain.Execution;
import com.rnexchange.domain.Instrument;
import com.rnexchange.domain.Order;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.service.dto.ExecutionDTO;
import com.rnexchange.service.dto.InstrumentDTO;
import com.rnexchange.service.dto.OrderDTO;
import com.rnexchange.service.dto.TradingAccountDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Execution} and its DTO {@link ExecutionDTO}.
 */
@Mapper(componentModel = "spring")
public interface ExecutionMapper extends EntityMapper<ExecutionDTO, Execution> {
    @Mapping(target = "order", source = "order", qualifiedByName = "orderId")
    @Mapping(target = "tradingAccount", source = "tradingAccount", qualifiedByName = "tradingAccountId")
    @Mapping(target = "instrument", source = "instrument", qualifiedByName = "instrumentId")
    ExecutionDTO toDto(Execution s);

    @Named("orderId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    OrderDTO toDtoOrderId(Order order);

    @Named("tradingAccountId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    TradingAccountDTO toDtoTradingAccountId(TradingAccount tradingAccount);

    @Named("instrumentId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    InstrumentDTO toDtoInstrumentId(Instrument instrument);
}
