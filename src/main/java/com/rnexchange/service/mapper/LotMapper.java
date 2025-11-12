package com.rnexchange.service.mapper;

import com.rnexchange.domain.Lot;
import com.rnexchange.domain.Position;
import com.rnexchange.service.dto.LotDTO;
import com.rnexchange.service.dto.PositionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Lot} and its DTO {@link LotDTO}.
 */
@Mapper(componentModel = "spring")
public interface LotMapper extends EntityMapper<LotDTO, Lot> {
    @Mapping(target = "position", source = "position", qualifiedByName = "positionId")
    LotDTO toDto(Lot s);

    @Named("positionId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PositionDTO toDtoPositionId(Position position);
}
