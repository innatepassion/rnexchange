package com.rnexchange.service.settlement;

import com.rnexchange.domain.*;
import com.rnexchange.domain.enumeration.LedgerEntryType;
import com.rnexchange.repository.*;
import com.rnexchange.security.SecurityUtils;
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
 * Implementation of StatementService for assembling per-account daily statements.
 */
@Service
@Transactional
public class StatementServiceImpl implements StatementService {

    private static final Logger LOG = LoggerFactory.getLogger(StatementServiceImpl.class);
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final LedgerEntryRepository ledgerEntryRepository;
    private final TradingAccountRepository tradingAccountRepository;
    private final TraderProfileRepository traderProfileRepository;
    private final ReportLinkRepository reportLinkRepository;

    public StatementServiceImpl(
        LedgerEntryRepository ledgerEntryRepository,
        TradingAccountRepository tradingAccountRepository,
        TraderProfileRepository traderProfileRepository,
        ReportLinkRepository reportLinkRepository
    ) {
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.tradingAccountRepository = tradingAccountRepository;
        this.traderProfileRepository = traderProfileRepository;
        this.reportLinkRepository = reportLinkRepository;
    }

    /**
     * Get all statements for the authenticated trader.
     */
    @Transactional(readOnly = true)
    public List<StatementSummary> getStatementsForTrader(LocalDate fromDate, LocalDate toDate) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AccessDeniedException("User not authenticated"));

        TraderProfile trader = traderProfileRepository
            .findOneByUserLogin(login)
            .orElseThrow(() -> new AccessDeniedException("Trader profile not found for user: " + login));

        // Get all trading accounts for this trader
        List<TradingAccount> accounts = tradingAccountRepository
            .findAll()
            .stream()
            .filter(ta -> ta.getTrader() != null && ta.getTrader().getId().equals(trader.getId()))
            .collect(Collectors.toList());

        if (accounts.isEmpty()) {
            return Collections.emptyList();
        }

        // Get all report links for these accounts in the date range
        List<ReportLink> reportLinks = reportLinkRepository
            .findAll()
            .stream()
            .filter(rl -> rl.getTradingAccount() != null && accounts.contains(rl.getTradingAccount()))
            .filter(rl -> {
                if (fromDate != null && rl.getRefDate().isBefore(fromDate)) return false;
                if (toDate != null && rl.getRefDate().isAfter(toDate)) return false;
                return true;
            })
            .collect(Collectors.toList());

        // Group by account and date
        Map<TradingAccount, Map<LocalDate, ReportLink>> linksByAccountAndDate = new HashMap<>();
        for (ReportLink link : reportLinks) {
            linksByAccountAndDate.computeIfAbsent(link.getTradingAccount(), k -> new HashMap<>()).put(link.getRefDate(), link);
        }

        List<StatementSummary> statements = new ArrayList<>();
        for (TradingAccount account : accounts) {
            Map<LocalDate, ReportLink> dateLinks = linksByAccountAndDate.getOrDefault(account, Collections.emptyMap());
            Set<LocalDate> statementDates = new HashSet<>(dateLinks.keySet());

            // Also include dates with ledger activity even if no report link exists
            List<LedgerEntry> allEntries = ledgerEntryRepository
                .findAll()
                .stream()
                .filter(le -> le.getTradingAccount() != null && le.getTradingAccount().getId().equals(account.getId()))
                .filter(le -> {
                    LocalDate entryDate = le.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (fromDate != null && entryDate.isBefore(fromDate)) return false;
                    if (toDate != null && entryDate.isAfter(toDate)) return false;
                    return true;
                })
                .collect(Collectors.toList());

            for (LedgerEntry entry : allEntries) {
                LocalDate entryDate = entry.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                statementDates.add(entryDate);
            }

            // Create statement summary for each date
            for (LocalDate date : statementDates) {
                StatementSummary summary = buildStatementSummary(account, date);
                if (summary != null) {
                    statements.add(summary);
                }
            }
        }

        // Sort by date descending
        statements.sort((a, b) -> b.getRefDate().compareTo(a.getRefDate()));

        return statements;
    }

    /**
     * Build a statement summary for a specific account and date.
     */
    @Transactional(readOnly = true)
    @Override
    public StatementSummary buildStatementSummary(TradingAccount account, LocalDate refDate) {
        // Get all ledger entries for this account on this date
        List<LedgerEntry> dayEntries = ledgerEntryRepository
            .findAll()
            .stream()
            .filter(le -> le.getTradingAccount() != null && le.getTradingAccount().getId().equals(account.getId()))
            .filter(le -> {
                LocalDate entryDate = le.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                return entryDate.equals(refDate);
            })
            .sorted(Comparator.comparing(LedgerEntry::getCreatedAt))
            .collect(Collectors.toList());

        if (dayEntries.isEmpty()) {
            return null;
        }

        // Calculate opening balance (balance at start of day)
        BigDecimal openingBalance = calculateOpeningBalance(account, refDate);

        // Separate EOD MTM entries from other entries
        BigDecimal eodMtmPnl = ZERO;
        BigDecimal netCashFlows = ZERO;

        for (LedgerEntry entry : dayEntries) {
            if (entry.getType() == LedgerEntryType.EOD_MTM_CREDIT) {
                eodMtmPnl = eodMtmPnl.add(entry.getAmount());
            } else if (entry.getType() == LedgerEntryType.EOD_MTM_DEBIT) {
                eodMtmPnl = eodMtmPnl.subtract(entry.getAmount());
            } else if (entry.getType() == LedgerEntryType.CREDIT) {
                netCashFlows = netCashFlows.add(entry.getAmount());
            } else if (entry.getType() == LedgerEntryType.DEBIT) {
                netCashFlows = netCashFlows.subtract(entry.getAmount());
            }
        }

        // Calculate closing balance
        BigDecimal closingBalance = openingBalance.add(netCashFlows).add(eodMtmPnl);

        StatementSummary summary = new StatementSummary();
        summary.setRefDate(refDate);
        summary.setTradingAccountId(account.getId());
        summary.setTradingAccountLabel(account.getTrader() != null ? account.getTrader().getDisplayName() : "Account " + account.getId());
        summary.setOpeningBalance(openingBalance);
        summary.setNetCashFlows(netCashFlows);
        summary.setEodMtmPnl(eodMtmPnl);
        summary.setClosingBalance(closingBalance);

        // Generate statement ID (use a combination of account ID and date)
        // Format: accountId * 1000000 + (epochDay % 1000000)
        // This allows for dates within ~2739 years from epoch
        long epochDay = refDate.toEpochDay();
        summary.setId(account.getId() * 1000000L + (epochDay % 1000000L));

        // Set HTML URL
        summary.setHtmlUrl("/api/statements/" + summary.getId() + "/html");

        return summary;
    }

    /**
     * Get statement HTML for a specific statement ID.
     */
    @Transactional(readOnly = true)
    public String getStatementHtml(Long statementId) {
        // Extract account ID and date from statement ID
        // Format: accountId * 1000000 + (epochDay % 1000000)
        Long accountId = statementId / 1000000L;
        long epochDayMod = statementId % 1000000L;

        // Find the actual date by searching for report links or ledger entries
        // For now, we'll search recent dates (within last 2 years)
        LocalDate refDate = null;
        TradingAccount account = tradingAccountRepository
            .findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Trading account not found: " + accountId));

        // Try to find the date from report links
        List<ReportLink> links = reportLinkRepository
            .findAll()
            .stream()
            .filter(rl -> rl.getTradingAccount() != null && rl.getTradingAccount().getId().equals(accountId))
            .filter(rl -> {
                long linkEpochDay = rl.getRefDate().toEpochDay();
                return (linkEpochDay % 1000000L) == epochDayMod;
            })
            .collect(Collectors.toList());

        if (!links.isEmpty()) {
            refDate = links.get(0).getRefDate();
        } else {
            // Fallback: search ledger entries
            List<LedgerEntry> entries = ledgerEntryRepository
                .findAll()
                .stream()
                .filter(le -> le.getTradingAccount() != null && le.getTradingAccount().getId().equals(accountId))
                .filter(le -> {
                    LocalDate entryDate = le.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                    return (entryDate.toEpochDay() % 1000000L) == epochDayMod;
                })
                .limit(1)
                .collect(Collectors.toList());

            if (!entries.isEmpty()) {
                refDate = entries.get(0).getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        }

        if (refDate == null) {
            throw new IllegalArgumentException("Could not determine date for statement ID: " + statementId);
        }

        // Verify ownership
        verifyOwnership(account);

        return generateHtmlStatement(account, refDate);
    }

    /**
     * Verify that the current user owns the trading account.
     */
    private void verifyOwnership(TradingAccount account) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AccessDeniedException("User not authenticated"));

        TraderProfile trader = traderProfileRepository
            .findOneByUserLogin(login)
            .orElseThrow(() -> new AccessDeniedException("Trader profile not found"));

        if (account.getTrader() == null || !account.getTrader().getId().equals(trader.getId())) {
            throw new AccessDeniedException("Access denied: statement does not belong to trader");
        }
    }

    /**
     * Calculate opening balance for an account at the start of a date.
     */
    private BigDecimal calculateOpeningBalance(TradingAccount account, LocalDate refDate) {
        // Get the last balance before this date
        Instant startOfDay = refDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        List<LedgerEntry> priorEntries = ledgerEntryRepository
            .findAll()
            .stream()
            .filter(le -> le.getTradingAccount() != null && le.getTradingAccount().getId().equals(account.getId()))
            .filter(le -> le.getCreatedAt().isBefore(startOfDay))
            .sorted(Comparator.comparing(LedgerEntry::getCreatedAt).reversed())
            .collect(Collectors.toList());

        if (priorEntries.isEmpty()) {
            // No prior entries, use account balance minus today's entries
            return account.getBalance();
        }

        // Use the balanceAfter from the last entry before this date
        LedgerEntry lastEntry = priorEntries.get(0);
        if (lastEntry.getBalanceAfter() != null) {
            return lastEntry.getBalanceAfter();
        }

        // Fallback: calculate from account balance
        return account.getBalance();
    }

    /**
     * Generate HTML statement for an account and date.
     */
    private String generateHtmlStatement(TradingAccount account, LocalDate refDate) {
        StatementSummary summary = buildStatementSummary(account, refDate);
        if (summary == null) {
            throw new IllegalArgumentException("No statement data found for account " + account.getId() + " on " + refDate);
        }

        // Get all ledger entries for this date
        List<LedgerEntry> dayEntries = ledgerEntryRepository
            .findAll()
            .stream()
            .filter(le -> le.getTradingAccount() != null && le.getTradingAccount().getId().equals(account.getId()))
            .filter(le -> {
                LocalDate entryDate = le.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                return entryDate.equals(refDate);
            })
            .sorted(Comparator.comparing(LedgerEntry::getCreatedAt))
            .collect(Collectors.toList());

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<title>Daily Statement - ").append(refDate).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("h1 { color: #333; }\n");
        html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        html.append("th { background-color: #f2f2f2; }\n");
        html.append(".disclaimer { background-color: #fff3cd; padding: 10px; margin: 20px 0; border-left: 4px solid #ffc107; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        html.append("<h1>Daily Statement</h1>\n");
        html.append("<p><strong>Account:</strong> ").append(summary.getTradingAccountLabel()).append("</p>\n");
        html.append("<p><strong>Date:</strong> ").append(refDate).append("</p>\n");

        html.append("<div class=\"disclaimer\">\n");
        html.append("<strong>Simulated Environment Notice:</strong><br/>\n");
        html.append("This statement is generated from simulated EOD settlement data. ");
        html.append("Prices and P&L are from internal mock feeds and are for training purposes only.\n");
        html.append("</div>\n");

        html.append("<h2>Summary</h2>\n");
        html.append("<table>\n");
        html.append("<tr><th>Opening Balance</th><td>").append(summary.getOpeningBalance()).append("</td></tr>\n");
        html.append("<tr><th>Net Cash Flows</th><td>").append(summary.getNetCashFlows()).append("</td></tr>\n");
        html.append("<tr><th>EOD MTM P&L</th><td>").append(summary.getEodMtmPnl()).append("</td></tr>\n");
        html.append("<tr><th>Closing Balance</th><td><strong>").append(summary.getClosingBalance()).append("</strong></td></tr>\n");
        html.append("</table>\n");

        html.append("<h2>Ledger Entries</h2>\n");
        html.append("<table>\n");
        html.append("<tr><th>Time</th><th>Type</th><th>Amount</th><th>Description</th><th>Balance After</th></tr>\n");

        for (LedgerEntry entry : dayEntries) {
            html.append("<tr>\n");
            html.append("<td>").append(entry.getCreatedAt()).append("</td>\n");
            html.append("<td>").append(entry.getType()).append("</td>\n");
            html.append("<td>").append(entry.getAmount()).append("</td>\n");
            html.append("<td>").append(entry.getDescription() != null ? entry.getDescription() : "").append("</td>\n");
            html.append("<td>").append(entry.getBalanceAfter() != null ? entry.getBalanceAfter() : "").append("</td>\n");
            html.append("</tr>\n");
        }

        html.append("</table>\n");
        html.append("</body>\n");
        html.append("</html>\n");

        return html.toString();
    }
}
