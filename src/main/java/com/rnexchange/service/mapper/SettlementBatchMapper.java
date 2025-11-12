package com.rnexchange.service.mapper;

import com.rnexchange.domain.Exchange;
import com.rnexchange.domain.SettlementBatch;
import com.rnexchange.service.dto.ExchangeDTO;
import com.rnexchange.service.dto.SettlementBatchDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link SettlementBatch} and its DTO {@link SettlementBatchDTO}.
 */
@Mapper(componentModel = "spring")
public interface SettlementBatchMapper extends EntityMapper<SettlementBatchDTO, SettlementBatch> {
    @Mapping(target = "exchange", source = "exchange", qualifiedByName = "exchangeCode")
    SettlementBatchDTO toDto(SettlementBatch s);

    @Named("exchangeCode")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    ExchangeDTO toDtoExchangeCode(Exchange exchange);
}
