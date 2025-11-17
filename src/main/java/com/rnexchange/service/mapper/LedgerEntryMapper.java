package com.rnexchange.service.mapper;

import com.rnexchange.domain.LedgerEntry;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.service.dto.LedgerEntryDTO;
import com.rnexchange.service.dto.TradingAccountDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link LedgerEntry} and its DTO {@link LedgerEntryDTO}.
 */
@Mapper(componentModel = "spring", uses = { TradingAccountMapper.class })
public interface LedgerEntryMapper extends EntityMapper<LedgerEntryDTO, LedgerEntry> {
    @Mapping(target = "tradingAccount", source = "tradingAccount")
    LedgerEntryDTO toDto(LedgerEntry s);
}
