package com.rnexchange.repository.projection;

import com.rnexchange.domain.enumeration.AssetClass;
import com.rnexchange.domain.enumeration.Currency;
import java.math.BigDecimal;

public record BrokerInstrumentRow(
    String symbol,
    String name,
    String exchangeCode,
    AssetClass assetClass,
    BigDecimal tickSize,
    Long lotSize,
    Currency currency
) {}
