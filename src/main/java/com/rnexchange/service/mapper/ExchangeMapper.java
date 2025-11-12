package com.rnexchange.service.mapper;

import com.rnexchange.domain.Exchange;
import com.rnexchange.service.dto.ExchangeDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Exchange} and its DTO {@link ExchangeDTO}.
 */
@Mapper(componentModel = "spring")
public interface ExchangeMapper extends EntityMapper<ExchangeDTO, Exchange> {}
