package com.rnexchange.service.mapper;

import com.rnexchange.domain.Broker;
import com.rnexchange.domain.TraderProfile;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.service.dto.BrokerDTO;
import com.rnexchange.service.dto.TraderProfileDTO;
import com.rnexchange.service.dto.TradingAccountDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TradingAccount} and its DTO {@link TradingAccountDTO}.
 */
@Mapper(componentModel = "spring")
public interface TradingAccountMapper extends EntityMapper<TradingAccountDTO, TradingAccount> {
    @Mapping(target = "broker", source = "broker", qualifiedByName = "brokerCode")
    @Mapping(target = "trader", source = "trader", qualifiedByName = "traderProfileDisplayName")
    TradingAccountDTO toDto(TradingAccount s);

    @Named("brokerCode")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    BrokerDTO toDtoBrokerCode(Broker broker);

    @Named("traderProfileDisplayName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "displayName", source = "displayName")
    TraderProfileDTO toDtoTraderProfileDisplayName(TraderProfile traderProfile);
}
