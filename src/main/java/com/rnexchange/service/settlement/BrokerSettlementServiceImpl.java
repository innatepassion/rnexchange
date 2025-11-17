package com.rnexchange.service.settlement;

import com.rnexchange.domain.*;
import com.rnexchange.domain.enumeration.LedgerEntryType;
import com.rnexchange.repository.*;
import com.rnexchange.security.SecurityUtils;
import com.rnexchange.service.dto.BrokerSettlementSummary;
import com.rnexchange.service.dto.StatementSummary;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of BrokerSettlementService for aggregating broker-level settlement summaries.
 */
@Service
@Transactional
public class BrokerSettlementServiceImpl implements BrokerSettlementService {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerSettlementServiceImpl.class);
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final BrokerDeskRepository brokerDeskRepository;
    private final TradingAccountRepository tradingAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final StatementService statementService;
    private final ReportLinkRepository reportLinkRepository;

    public BrokerSettlementServiceImpl(
        BrokerDeskRepository brokerDeskRepository,
        TradingAccountRepository tradingAccountRepository,
        LedgerEntryRepository ledgerEntryRepository,
        StatementService statementService,
        ReportLinkRepository reportLinkRepository
    ) {
        this.brokerDeskRepository = brokerDeskRepository;
        this.tradingAccountRepository = tradingAccountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.statementService = statementService;
        this.reportLinkRepository = reportLinkRepository;
    }

    /**
     * Get broker-level settlement summaries for the authenticated broker admin.
     */
    @Transactional(readOnly = true)
    @Override
    public List<BrokerSettlementSummary> getBrokerSettlements(LocalDate fromDate, LocalDate toDate) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AccessDeniedException("User not authenticated"));

        BrokerDesk brokerDesk = brokerDeskRepository
            .findByUserLogin(login)
            .orElseThrow(() -> new AccessDeniedException("Broker desk not found for user: " + login));

        if (brokerDesk.getBroker() == null) {
            throw new AccessDeniedException("Broker desk is not associated with a broker");
        }

        Broker broker = brokerDesk.getBroker();
        Long brokerId = broker.getId();

        // Get all trading accounts for this broker
        List<TradingAccount> accounts = tradingAccountRepository
            .findAll()
            .stream()
            .filter(ta -> ta.getBroker() != null && ta.getBroker().getId().equals(brokerId))
            .collect(Collectors.toList());

        if (accounts.isEmpty()) {
            return Collections.emptyList();
        }

        // Get all unique dates with activity for these accounts
        Set<LocalDate> settlementDates = new HashSet<>();

        // Get dates from report links
        List<ReportLink> reportLinks = reportLinkRepository
            .findAll()
            .stream()
            .filter(rl -> rl.getBroker() != null && rl.getBroker().getId().equals(brokerId))
            .filter(rl -> rl.getReportType() != null && rl.getReportType().equals("BROKER_SUMMARY"))
            .filter(rl -> {
                if (fromDate != null && rl.getRefDate().isBefore(fromDate)) return false;
                if (toDate != null && rl.getRefDate().isAfter(toDate)) return false;
                return true;
            })
            .collect(Collectors.toList());

        for (ReportLink link : reportLinks) {
            settlementDates.add(link.getRefDate());
        }

        // Also include dates with ledger activity
        List<LedgerEntry> allEntries = ledgerEntryRepository
            .findAll()
            .stream()
            .filter(le -> le.getTradingAccount() != null && accounts.contains(le.getTradingAccount()))
            .filter(le -> {
                LocalDate entryDate = le.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                if (fromDate != null && entryDate.isBefore(fromDate)) return false;
                if (toDate != null && entryDate.isAfter(toDate)) return false;
                return true;
            })
            .collect(Collectors.toList());

        for (LedgerEntry entry : allEntries) {
            LocalDate entryDate = entry.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
            settlementDates.add(entryDate);
        }

        // Build summaries for each date
        List<BrokerSettlementSummary> summaries = new ArrayList<>();
        for (LocalDate date : settlementDates) {
            BrokerSettlementSummary summary = buildBrokerSummary(broker, accounts, date);
            if (summary != null) {
                summaries.add(summary);
            }
        }

        // Sort by date descending
        summaries.sort((a, b) -> b.getRefDate().compareTo(a.getRefDate()));

        return summaries;
    }

    /**
     * Build a broker settlement summary for a specific broker and date.
     */
    @Transactional(readOnly = true)
    private BrokerSettlementSummary buildBrokerSummary(Broker broker, List<TradingAccount> accounts, LocalDate refDate) {
        BigDecimal totalOpeningBalance = ZERO;
        BigDecimal totalClosingBalance = ZERO;
        BigDecimal totalEodMtmPnl = ZERO;
        int clientCount = 0;

        // Aggregate from individual statements
        for (TradingAccount account : accounts) {
            StatementSummary statement = statementService.buildStatementSummary(account, refDate);
            if (statement != null) {
                totalOpeningBalance = totalOpeningBalance.add(statement.getOpeningBalance());
                totalClosingBalance = totalClosingBalance.add(statement.getClosingBalance());
                totalEodMtmPnl = totalEodMtmPnl.add(statement.getEodMtmPnl());
                clientCount++;
            }
        }

        if (clientCount == 0) {
            return null;
        }

        BrokerSettlementSummary summary = new BrokerSettlementSummary();
        summary.setRefDate(refDate);
        summary.setBrokerId(broker.getId());
        summary.setBrokerName(broker.getName());
        summary.setTotalClientCount(clientCount);
        summary.setTotalOpeningBalance(totalOpeningBalance);
        summary.setTotalClosingBalance(totalClosingBalance);
        summary.setTotalEodMtmPnl(totalEodMtmPnl);

        // Generate summary URL
        summary.setSummaryUrl("/api/broker/settlements/" + broker.getId() + "/summary?date=" + refDate);

        return summary;
    }

    /**
     * Get HTML summary for a specific broker and date.
     */
    @Transactional(readOnly = true)
    @Override
    public String getBrokerSummaryHtml(Long brokerId, LocalDate refDate) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AccessDeniedException("User not authenticated"));

        BrokerDesk brokerDesk = brokerDeskRepository
            .findByUserLogin(login)
            .orElseThrow(() -> new AccessDeniedException("Broker desk not found for user: " + login));

        if (brokerDesk.getBroker() == null || !brokerDesk.getBroker().getId().equals(brokerId)) {
            throw new AccessDeniedException("Access denied: broker does not belong to authenticated user");
        }

        Broker broker = brokerDesk.getBroker();

        // Get all trading accounts for this broker
        List<TradingAccount> accounts = tradingAccountRepository
            .findAll()
            .stream()
            .filter(ta -> ta.getBroker() != null && ta.getBroker().getId().equals(brokerId))
            .collect(Collectors.toList());

        BrokerSettlementSummary summary = buildBrokerSummary(broker, accounts, refDate);
        if (summary == null) {
            throw new IllegalArgumentException("No settlement data found for broker " + brokerId + " on " + refDate);
        }

        // Generate HTML
        StringBuilder html = new StringBuilder();
        html.append(HtmlReportUtils.htmlHeader("Broker Settlement Summary - " + refDate));

        html.append("<h1>Broker Settlement Summary</h1>\n");
        html.append("<p><strong>Broker:</strong> ").append(broker.getName()).append("</p>\n");
        html.append("<p><strong>Date:</strong> ").append(refDate).append("</p>\n");

        html.append(HtmlReportUtils.SIMULATED_ENVIRONMENT_DISCLAIMER);

        html.append("<h2>Summary Totals</h2>\n");
        html.append("<table>\n");
        html.append(HtmlReportUtils.tableRow("Total Client Count", summary.getTotalClientCount()));
        html.append(HtmlReportUtils.tableRow("Total Opening Balance", summary.getTotalOpeningBalance()));
        html.append(HtmlReportUtils.tableRow("Total Closing Balance", summary.getTotalClosingBalance()));
        html.append(HtmlReportUtils.tableRow("Total EOD MTM P&L", summary.getTotalEodMtmPnl()));
        html.append("</table>\n");

        html.append("<h2>Client Statements</h2>\n");
        html.append("<p>Individual client statements can be accessed through the broker portal.</p>\n");

        html.append(HtmlReportUtils.htmlFooter());

        return html.toString();
    }
}
