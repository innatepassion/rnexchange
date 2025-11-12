package com.rnexchange.service.mapper;

import com.rnexchange.domain.Broker;
import com.rnexchange.domain.Exchange;
import com.rnexchange.service.dto.BrokerDTO;
import com.rnexchange.service.dto.ExchangeDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Broker} and its DTO {@link BrokerDTO}.
 */
@Mapper(componentModel = "spring")
public interface BrokerMapper extends EntityMapper<BrokerDTO, Broker> {
    @Mapping(target = "exchange", source = "exchange", qualifiedByName = "exchangeCode")
    BrokerDTO toDto(Broker s);

    @Named("exchangeCode")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    ExchangeDTO toDtoExchangeCode(Exchange exchange);
}
