package com.rnexchange.service.mapper;

import com.rnexchange.domain.Exchange;
import com.rnexchange.domain.MarketHoliday;
import com.rnexchange.service.dto.ExchangeDTO;
import com.rnexchange.service.dto.MarketHolidayDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link MarketHoliday} and its DTO {@link MarketHolidayDTO}.
 */
@Mapper(componentModel = "spring")
public interface MarketHolidayMapper extends EntityMapper<MarketHolidayDTO, MarketHoliday> {
    @Mapping(target = "exchange", source = "exchange", qualifiedByName = "exchangeCode")
    MarketHolidayDTO toDto(MarketHoliday s);

    @Named("exchangeCode")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    ExchangeDTO toDtoExchangeCode(Exchange exchange);
}
