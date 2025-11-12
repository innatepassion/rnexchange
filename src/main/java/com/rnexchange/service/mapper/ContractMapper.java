package com.rnexchange.service.mapper;

import com.rnexchange.domain.Contract;
import com.rnexchange.domain.Instrument;
import com.rnexchange.service.dto.ContractDTO;
import com.rnexchange.service.dto.InstrumentDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Contract} and its DTO {@link ContractDTO}.
 */
@Mapper(componentModel = "spring")
public interface ContractMapper extends EntityMapper<ContractDTO, Contract> {
    @Mapping(target = "instrument", source = "instrument", qualifiedByName = "instrumentSymbol")
    ContractDTO toDto(Contract s);

    @Named("instrumentSymbol")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "symbol", source = "symbol")
    InstrumentDTO toDtoInstrumentSymbol(Instrument instrument);
}
