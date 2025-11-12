package com.rnexchange.service.mapper;

import com.rnexchange.domain.LedgerEntry;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.service.dto.LedgerEntryDTO;
import com.rnexchange.service.dto.TradingAccountDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link LedgerEntry} and its DTO {@link LedgerEntryDTO}.
 */
@Mapper(componentModel = "spring")
public interface LedgerEntryMapper extends EntityMapper<LedgerEntryDTO, LedgerEntry> {
    @Mapping(target = "tradingAccount", source = "tradingAccount", qualifiedByName = "tradingAccountId")
    LedgerEntryDTO toDto(LedgerEntry s);

    @Named("tradingAccountId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    TradingAccountDTO toDtoTradingAccountId(TradingAccount tradingAccount);
}
