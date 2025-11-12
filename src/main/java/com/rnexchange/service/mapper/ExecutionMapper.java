package com.rnexchange.service.mapper;

import com.rnexchange.domain.Execution;
import com.rnexchange.domain.Order;
import com.rnexchange.service.dto.ExecutionDTO;
import com.rnexchange.service.dto.OrderDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Execution} and its DTO {@link ExecutionDTO}.
 */
@Mapper(componentModel = "spring")
public interface ExecutionMapper extends EntityMapper<ExecutionDTO, Execution> {
    @Mapping(target = "order", source = "order", qualifiedByName = "orderId")
    ExecutionDTO toDto(Execution s);

    @Named("orderId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    OrderDTO toDtoOrderId(Order order);
}
