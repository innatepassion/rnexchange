package com.rnexchange.service.mapper;

import com.rnexchange.domain.RiskAlert;
import com.rnexchange.domain.TraderProfile;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.service.dto.RiskAlertDTO;
import com.rnexchange.service.dto.TraderProfileDTO;
import com.rnexchange.service.dto.TradingAccountDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link RiskAlert} and its DTO {@link RiskAlertDTO}.
 */
@Mapper(componentModel = "spring")
public interface RiskAlertMapper extends EntityMapper<RiskAlertDTO, RiskAlert> {
    @Mapping(target = "tradingAccount", source = "tradingAccount", qualifiedByName = "tradingAccountId")
    @Mapping(target = "trader", source = "trader", qualifiedByName = "traderProfileDisplayName")
    RiskAlertDTO toDto(RiskAlert s);

    @Named("tradingAccountId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    TradingAccountDTO toDtoTradingAccountId(TradingAccount tradingAccount);

    @Named("traderProfileDisplayName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "displayName", source = "displayName")
    TraderProfileDTO toDtoTraderProfileDisplayName(TraderProfile traderProfile);
}
