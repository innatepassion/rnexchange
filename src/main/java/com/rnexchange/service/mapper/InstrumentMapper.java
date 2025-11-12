package com.rnexchange.service.mapper;

import com.rnexchange.domain.Exchange;
import com.rnexchange.domain.Instrument;
import com.rnexchange.service.dto.ExchangeDTO;
import com.rnexchange.service.dto.InstrumentDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Instrument} and its DTO {@link InstrumentDTO}.
 */
@Mapper(componentModel = "spring")
public interface InstrumentMapper extends EntityMapper<InstrumentDTO, Instrument> {
    @Mapping(target = "exchange", source = "exchange", qualifiedByName = "exchangeCode")
    InstrumentDTO toDto(Instrument s);

    @Named("exchangeCode")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    ExchangeDTO toDtoExchangeCode(Exchange exchange);
}
