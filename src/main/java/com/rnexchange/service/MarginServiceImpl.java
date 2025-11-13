package com.rnexchange.service;

import com.rnexchange.domain.Instrument;
import com.rnexchange.domain.MarginRule;
import com.rnexchange.domain.TradingAccount;
import com.rnexchange.domain.enumeration.AssetClass;
import com.rnexchange.repository.InstrumentRepository;
import com.rnexchange.repository.MarginRuleRepository;
import com.rnexchange.repository.TradingAccountRepository;
import com.rnexchange.service.dto.MarginAssessment;
import com.rnexchange.service.dto.TraderOrderRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MarginServiceImpl implements MarginService {

    private static final Logger LOG = LoggerFactory.getLogger(MarginServiceImpl.class);

    private final TradingAccountRepository tradingAccountRepository;
    private final InstrumentRepository instrumentRepository;
    private final MarginRuleRepository marginRuleRepository;

    public MarginServiceImpl(
        TradingAccountRepository tradingAccountRepository,
        InstrumentRepository instrumentRepository,
        MarginRuleRepository marginRuleRepository
    ) {
        this.tradingAccountRepository = tradingAccountRepository;
        this.instrumentRepository = instrumentRepository;
        this.marginRuleRepository = marginRuleRepository;
    }

    @Override
    public MarginAssessment evaluateMargin(TraderOrderRequest request) {
        Objects.requireNonNull(request, "Margin request must not be null");
        validateRequestValues(request);

        TradingAccount tradingAccount = tradingAccountRepository
            .findFirstByTrader_User_LoginOrderByIdAsc(request.getTraderLogin())
            .orElseThrow(() -> new IllegalStateException("Trading account not found for trader login " + request.getTraderLogin()));

        Instrument instrument = resolveInstrument(request);

        MarginRule marginRule = resolveMarginRule(instrument);

        BigDecimal notional = request.getPrice().multiply(request.getQuantity()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal initialRequirement = notional.multiply(defaultIfNull(marginRule.getInitialPct())).setScale(2, RoundingMode.HALF_UP);
        BigDecimal maintenanceRequirement = notional.multiply(defaultIfNull(marginRule.getMaintPct())).setScale(2, RoundingMode.HALF_UP);

        BigDecimal availableBalance = defaultIfNull(tradingAccount.getBalance()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal remainingBalance = availableBalance.subtract(initialRequirement).setScale(2, RoundingMode.HALF_UP);

        boolean sufficient = remainingBalance.compareTo(BigDecimal.ZERO) >= 0;
        MarginAssessment assessment = new MarginAssessment(
            initialRequirement,
            maintenanceRequirement,
            availableBalance,
            remainingBalance,
            sufficient
        );

        if (!sufficient) {
            String message =
                "Insufficient margin for trader %s on instrument %s: required %s, available %s".formatted(
                        request.getTraderLogin(),
                        request.getInstrumentSymbol(),
                        initialRequirement,
                        availableBalance
                    );
            LOG.warn(message);
            throw new InsufficientMarginException(message, assessment);
        }

        return assessment;
    }

    private void validateRequestValues(TraderOrderRequest request) {
        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive for margin evaluation");
        }
        if (request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive for margin evaluation");
        }
    }

    private MarginRule resolveMarginRule(Instrument instrument) {
        String exchangeCode = instrument.getExchange().getCode();
        String scope =
            switch (instrument.getAssetClass()) {
                case EQUITY -> exchangeCode + "_CASH";
                case COMMODITY -> exchangeCode + "_COMMODITY";
                default -> exchangeCode + "_" + instrument.getAssetClass().name();
            };
        return marginRuleRepository
            .findOneByExchange_CodeAndScope(exchangeCode, scope)
            .or(() -> fallbackDerivativeScope(exchangeCode, instrument.getAssetClass()))
            .orElseThrow(() -> new IllegalStateException("Margin rule not found for scope %s on exchange %s".formatted(scope, exchangeCode))
            );
    }

    private Optional<MarginRule> fallbackDerivativeScope(String exchangeCode, AssetClass assetClass) {
        if (assetClass == AssetClass.EQUITY) {
            return marginRuleRepository.findOneByExchange_CodeAndScope(exchangeCode, exchangeCode + "_FNO");
        }
        return Optional.empty();
    }

    private Instrument resolveInstrument(TraderOrderRequest request) {
        if (request.getInstrumentSymbol() != null) {
            Optional<Instrument> bySymbol = instrumentRepository.findOneBySymbol(request.getInstrumentSymbol());
            if (bySymbol.isPresent()) {
                return bySymbol.get();
            }
        }
        if (request.getInstrumentId() != null) {
            return instrumentRepository
                .findById(request.getInstrumentId())
                .orElseThrow(() ->
                    new IllegalStateException(
                        "Instrument not found for id " + request.getInstrumentId() + " (symbol=" + request.getInstrumentSymbol() + ")"
                    )
                );
        }
        throw new IllegalStateException("Instrument not found for symbol " + request.getInstrumentSymbol());
    }

    private BigDecimal defaultIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
