package com.rnexchange.service.mapper;

import com.rnexchange.domain.Broker;
import com.rnexchange.domain.BrokerDesk;
import com.rnexchange.domain.User;
import com.rnexchange.service.dto.BrokerDTO;
import com.rnexchange.service.dto.BrokerDeskDTO;
import com.rnexchange.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link BrokerDesk} and its DTO {@link BrokerDeskDTO}.
 */
@Mapper(componentModel = "spring")
public interface BrokerDeskMapper extends EntityMapper<BrokerDeskDTO, BrokerDesk> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    @Mapping(target = "broker", source = "broker", qualifiedByName = "brokerCode")
    BrokerDeskDTO toDto(BrokerDesk s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("brokerCode")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    BrokerDTO toDtoBrokerCode(Broker broker);
}
