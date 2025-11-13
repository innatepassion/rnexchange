package com.rnexchange.service.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.rnexchange.IntegrationTest;
import com.rnexchange.domain.Exchange;
import com.rnexchange.domain.Instrument;
import com.rnexchange.domain.MarginRule;
import com.rnexchange.domain.MarketHoliday;
import com.rnexchange.domain.TraderProfile;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.domain.enumeration.AccountStatus;
import com.rnexchange.domain.enumeration.AssetClass;
import com.rnexchange.domain.enumeration.Currency;
import com.rnexchange.domain.enumeration.ExchangeStatus;
import com.rnexchange.domain.enumeration.KycStatus;
import com.rnexchange.service.seed.dto.BaselineSeedRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
class BaselineSeedServiceIT extends AbstractBaselineSeedIT {

    private static final Set<String> EXPECTED_EXCHANGES = Set.of("NSE", "BSE", "MCX");
    private static final Map<String, BigDecimal> EXPECTED_TICK_SIZES = Map.ofEntries(
        Map.entry("NSE:RELIANCE", new BigDecimal("0.05")),
        Map.entry("NSE:HDFCBANK", new BigDecimal("0.05")),
        Map.entry("NSE:INFY", new BigDecimal("0.05")),
        Map.entry("NSE:TCS", new BigDecimal("0.05")),
        Map.entry("NSE:NIFTY50", new BigDecimal("0.05")),
        Map.entry("BSE:RELIANCE_BSE", new BigDecimal("0.05")),
        Map.entry("BSE:SBIN_BSE", new BigDecimal("0.05")),
        Map.entry("MCX:GOLD", new BigDecimal("1.0")),
        Map.entry("MCX:CRUDEOIL", new BigDecimal("1.0")),
        Map.entry("MCX:SILVER", new BigDecimal("1.0"))
    );

    private static final Map<String, Long> EXPECTED_LOT_SIZES = Map.ofEntries(
        Map.entry("MCX:CRUDEOIL", 10L),
        Map.entry("MCX:GOLD", 1L),
        Map.entry("MCX:SILVER", 1L),
        Map.entry("NSE:NIFTY50", 1L)
    );

    private static final Map<String, LocalDate> EXPECTED_HOLIDAYS = Map.of(
        "NSE",
        LocalDate.parse("2025-12-31"),
        "BSE",
        LocalDate.parse("2026-01-26"),
        "MCX",
        LocalDate.parse("2025-12-12")
    );

    @Autowired
    private BaselineSeedService baselineSeedService;

    @Test
    @Transactional
    void baselineSeed_populatesDeterministicDataset() {
        BaselineSeedRequest request = BaselineSeedRequest.builder().force(true).invocationId(UUID.randomUUID()).build();

        baselineSeedService.runBaselineSeedBlocking(request);

        assertThat(exchangeRepository.findAll())
            .hasSize(EXPECTED_EXCHANGES.size())
            .allSatisfy(exchange -> {
                assertThat(EXPECTED_EXCHANGES).contains(exchange.getCode());
                assertThat(exchange.getTimezone()).isEqualTo("Asia/Kolkata");
                assertThat(exchange.getStatus()).isEqualTo(ExchangeStatus.ACTIVE);
            });

        assertThat(instrumentRepository.findAll())
            .isNotEmpty()
            .allSatisfy(instrument -> {
                String key = instrument.getExchange().getCode() + ":" + instrument.getSymbol();
                assertThat(EXPECTED_TICK_SIZES).containsKey(key);
                assertThat(instrument.getTickSize()).isEqualByComparingTo(EXPECTED_TICK_SIZES.get(key));
                if (EXPECTED_LOT_SIZES.containsKey(key)) {
                    assertThat(instrument.getLotSize()).isEqualTo(EXPECTED_LOT_SIZES.get(key));
                } else {
                    assertThat(instrument.getLotSize()).isEqualTo(1L);
                }
                assertThat(instrument.getCurrency()).isEqualTo(Currency.INR);
                if (instrument.getExchange().getCode().equals("MCX")) {
                    assertThat(instrument.getAssetClass()).isEqualTo(AssetClass.COMMODITY);
                } else {
                    assertThat(instrument.getAssetClass()).isEqualTo(AssetClass.EQUITY);
                }
            });

        assertThat(marketHolidayRepository.findAll())
            .hasSize(EXPECTED_HOLIDAYS.size())
            .allSatisfy(holiday -> {
                String exchangeCode = holiday.getExchange().getCode();
                assertThat(EXPECTED_HOLIDAYS).containsKey(exchangeCode);
                assertThat(holiday.getTradeDate()).isEqualTo(EXPECTED_HOLIDAYS.get(exchangeCode));
            });

        assertThat(marginRuleRepository.findAll())
            .hasSize(2)
            .anySatisfy(rule -> {
                if ("NSE_CASH".equals(rule.getScope())) {
                    assertThat(rule.getInitialPct()).isEqualByComparingTo(new BigDecimal("0.20"));
                    assertThat(rule.getMaintPct()).isEqualByComparingTo(new BigDecimal("0.15"));
                } else if ("NSE_FNO".equals(rule.getScope())) {
                    assertThat(rule.getInitialPct()).isEqualByComparingTo(new BigDecimal("0.40"));
                    assertThat(rule.getMaintPct()).isEqualByComparingTo(new BigDecimal("0.30"));
                } else {
                    throw new AssertionError("Unexpected margin rule scope: " + rule.getScope());
                }
            });

        assertThat(traderProfileRepository.findAll())
            .hasSize(2)
            .allSatisfy(profile -> {
                assertThat(profile.getStatus()).isEqualTo(AccountStatus.ACTIVE);
                assertThat(profile.getKycStatus()).isEqualTo(KycStatus.APPROVED);
                assertThat(profile.getUser()).isNotNull();
            });

        assertThat(tradingAccountRepository.findAll())
            .hasSize(2)
            .allSatisfy(account -> {
                assertThat(account.getBalance()).isEqualByComparingTo("1000000.00");
                assertThat(account.getBaseCcy()).isEqualTo(Currency.INR);
                assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
            });
    }

    @Test
    @Transactional
    void baselineSeed_removesLegacyDemoDataAndIsIdempotent() {
        Exchange legacyExchange = new Exchange()
            .code("LEGACY")
            .name("Legacy Exchange")
            .timezone("Asia/Kolkata")
            .status(ExchangeStatus.ACTIVE);
        exchangeRepository.saveAndFlush(legacyExchange);

        Instrument legacyInstrument = new Instrument()
            .symbol("LEGACY_SYMBOL")
            .name("Legacy Instrument")
            .assetClass(AssetClass.EQUITY)
            .exchangeCode(legacyExchange.getCode())
            .tickSize(new BigDecimal("0.01"))
            .lotSize(1L)
            .currency(Currency.INR)
            .status("ACTIVE")
            .exchange(legacyExchange);
        instrumentRepository.saveAndFlush(legacyInstrument);

        BaselineSeedRequest request = BaselineSeedRequest.builder().force(true).invocationId(UUID.randomUUID()).build();
        baselineSeedService.runBaselineSeedBlocking(request);

        assertThat(exchangeRepository.findAll()).extracting(Exchange::getCode).doesNotContain("LEGACY");
        assertThat(instrumentRepository.findAll()).extracting(Instrument::getSymbol).doesNotContain("LEGACY_SYMBOL");

        Snapshot firstSnapshot = snapshot();
        baselineSeedService.runBaselineSeedBlocking(request);
        Snapshot secondSnapshot = snapshot();

        assertThat(secondSnapshot).isEqualTo(firstSnapshot);
    }

    private Snapshot snapshot() {
        return new Snapshot(
            exchangeRepository.count(),
            instrumentRepository.count(),
            marketHolidayRepository.count(),
            marginRuleRepository.count(),
            traderProfileRepository.findAll().stream().map(TraderProfile::getEmail).collect(Collectors.toSet()),
            tradingAccountRepository.count()
        );
    }

    private record Snapshot(
        long exchangeCount,
        long instrumentCount,
        long holidayCount,
        long marginRuleCount,
        Set<String> traderEmails,
        long tradingAccountCount
    ) {}
}
