package com.rnexchange.service.mapper;

import com.rnexchange.domain.Exchange;
import com.rnexchange.domain.MarginRule;
import com.rnexchange.service.dto.ExchangeDTO;
import com.rnexchange.service.dto.MarginRuleDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link MarginRule} and its DTO {@link MarginRuleDTO}.
 */
@Mapper(componentModel = "spring")
public interface MarginRuleMapper extends EntityMapper<MarginRuleDTO, MarginRule> {
    @Mapping(target = "exchange", source = "exchange", qualifiedByName = "exchangeCode")
    MarginRuleDTO toDto(MarginRule s);

    @Named("exchangeCode")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    ExchangeDTO toDtoExchangeCode(Exchange exchange);
}
