package com.rnexchange.service.mapper;

import com.rnexchange.domain.Exchange;
import com.rnexchange.domain.ExchangeOperator;
import com.rnexchange.domain.User;
import com.rnexchange.service.dto.ExchangeDTO;
import com.rnexchange.service.dto.ExchangeOperatorDTO;
import com.rnexchange.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ExchangeOperator} and its DTO {@link ExchangeOperatorDTO}.
 */
@Mapper(componentModel = "spring")
public interface ExchangeOperatorMapper extends EntityMapper<ExchangeOperatorDTO, ExchangeOperator> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    @Mapping(target = "exchange", source = "exchange", qualifiedByName = "exchangeCode")
    ExchangeOperatorDTO toDto(ExchangeOperator s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("exchangeCode")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    ExchangeDTO toDtoExchangeCode(Exchange exchange);
}
