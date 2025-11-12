package com.rnexchange.service.mapper;

import com.rnexchange.domain.CorporateAction;
import com.rnexchange.domain.Instrument;
import com.rnexchange.service.dto.CorporateActionDTO;
import com.rnexchange.service.dto.InstrumentDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CorporateAction} and its DTO {@link CorporateActionDTO}.
 */
@Mapper(componentModel = "spring")
public interface CorporateActionMapper extends EntityMapper<CorporateActionDTO, CorporateAction> {
    @Mapping(target = "instrument", source = "instrument", qualifiedByName = "instrumentSymbol")
    CorporateActionDTO toDto(CorporateAction s);

    @Named("instrumentSymbol")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "symbol", source = "symbol")
    InstrumentDTO toDtoInstrumentSymbol(Instrument instrument);
}
