package com.rnexchange.service.mapper;

import com.rnexchange.domain.Exchange;
import com.rnexchange.domain.ExchangeIntegration;
import com.rnexchange.service.dto.ExchangeDTO;
import com.rnexchange.service.dto.ExchangeIntegrationDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ExchangeIntegration} and its DTO {@link ExchangeIntegrationDTO}.
 */
@Mapper(componentModel = "spring")
public interface ExchangeIntegrationMapper extends EntityMapper<ExchangeIntegrationDTO, ExchangeIntegration> {
    @Mapping(target = "exchange", source = "exchange", qualifiedByName = "exchangeCode")
    ExchangeIntegrationDTO toDto(ExchangeIntegration s);

    @Named("exchangeCode")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    ExchangeDTO toDtoExchangeCode(Exchange exchange);
}
