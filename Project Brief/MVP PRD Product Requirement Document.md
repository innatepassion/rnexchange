Product Requirements Document (PRD)

Project: RNExchange – Multi-Asset Trading Simulator (India)
Version: 1.1 (Revised)
Stack: JHipster Monolith (Spring Boot + React + WebSockets + PostgreSQL)
Integration: Zerodha Kite API (Phase-M5)
Methodology: Agile + TDD + CI/CD

⸻

1. Vision

RNExchange simulates the full brokerage ecosystem for Indian markets (BSE, NSE, MCX) — enabling traders, brokers, and exchange operators to experience real-time trading, risk, and settlement workflows without real money.

Goals:
• Simulate full trade → position → P&L → settlement lifecycle.
• Build multi-role, governed access: Exchange → Broker → Trader.
• Use mock market data first, then switch to Kite feed.
• Provide a realistic back-office and risk environment.

⸻

2. Roles

Role Description
Exchange Operator Full system authority. Manages brokers, holidays, market sessions, settlements, and overrides.
Broker Admin Manages traders under their broker. Approves accounts, funds, risk, and EOD processing.
Trader Simulates trading on assigned broker; places orders, tracks portfolio, P&L, ledger.

⸻

3. MVP Scope

Includes:
✅ Full trading and account lifecycle for BSE/NSE/MCX (mock market).
✅ Governance hierarchy: Exchange > Broker > Trader.
✅ Realtime tick simulation & WebSocket broadcast.
✅ Portfolio, P&L, ledger, and settlement tracking.
✅ Basic margin and risk checks.
✅ Exchange control over users, brokers, and EOD.

Excludes until post-M5:
• Live Kite integration (only mock for M1–M4).
• Redis/Kafka/Elasticsearch infra.
• Audit explorer, leaderboards, complex analytics.

⸻

4. Functional Modules

Trader App
• Authentication: JWT (via built-in JHipster User).
• Market Watch: Subscribe to tick streams, create watchlists.
• Order Entry: Market/Limit/Stop/Stop-Limit, DAY/IOC/GTC.
• Positions: Realized & unrealized P&L, auto MTM refresh.
• Ledger: Deposits, trades, margin, and settlement journal.
• Reports: Contract note, ledger summary, MTM statement.

Broker Portal
• Dashboard: client count, exposure, pending settlements.
• Client Management: add/activate/suspend traders.
• Funds Journal: credit/debit balance.
• Risk Monitor: margin utilization, alerts, square-off queue.
• Settlement Control: run EOD, approve/repost results.

Exchange Console
• Broker lifecycle: create/activate/deactivate.
• Calendar & Holiday management.
• Market Simulation control (mock generator start/stop).
• Settlement & override capabilities.
• System overview (brokers, traders, session stats).

⸻

5. Architecture

Layer Technology
Frontend React (JHipster generated)
Backend Spring Boot REST + WebSocket (STOMP)
Database PostgreSQL (H2Disk for dev)
Auth JWT
Cache None (no Redis in MVP)
Jobs Spring Batch (for EOD)
Build Maven
Deployment Dockerized monolith
Tests JUnit5, Mockito, Cucumber, Cypress, Gatling

⸻

6. Market Data

Phase Description
M1–M4 Mock Generator: Simulated tick engine per symbol; configurable volatility and frequency; publishes via WebSocket.
M5 Kite API: Real tick stream; Redis pub/sub for broadcast; token auto-refresh.

⸻

7. Trading Lifecycle
   1. Trader places order (REST/WS).
   2. Validation: margin + account + symbol status.
   3. Match Engine fills order by price/time.
   4. Execution updates positions, ledger, P&L.
   5. Intraday auto-square-off at 15:20 IST.
   6. EOD settlement batches positions → MTM → ledger → statement.

⸻

8. Risk & Margin
   • Cash: 100% delivery; lower intraday margin.
   • Futures/Options: Per-contract initial/maintenance %.
   • Auto-Liquidation: Triggered when margin < threshold.
   • Risk Alerts: Margin breach, concentration limit, exposure alert.

⸻

9. Settlement
   • Apply settlement prices → calculate MTM → post ledger entries.
   • Generate daily HTML (PDF later) statements.
   • Broker and Exchange can rerun or override EOD.
   • Expiry handling for F&O (auto-close and P&L post).

⸻

10. Entities (Mapped in JDL)

Entity Description
Exchange Root system; owns brokers and integrations.
Broker Manages traders and desks.
TraderProfile Trader’s identity and preferences.
TradingAccount Account with margin and balances.
Instrument / Contract Market instruments and derivatives.
Order / Execution Order placement and fills.
Position / Lot P&L tracking and inventory.
LedgerEntry Financial journal of all events.
SettlementBatch EOD summary and processing status.
MarginRule / RiskAlert Risk policy definitions and alerts.

⸻

11. Reports

Report Audience Format
Contract Note Trader HTML
Broker Blotter Broker CSV
Ledger & P&L Trader, Broker CSV
Settlement Summary Exchange CSV

⸻

12. Non-Functional

Category Target
Users 1,000 concurrent traders
WS Updates 10,000/sec peak
Order Latency <250 ms
Security JWT, RBAC, SSL
Audit Minimal (EOD + critical ops)
Testing 90% backend, 80% frontend
Observability JHipster metrics + Prometheus

⸻

13. Methodology
    • Agile (2-week sprints)
    • TDD enforced: JUnit5, Mockito, Testcontainers.
    • Lint & Style: ESLint, Prettier, Checkstyle.
    • CI/CD: GitHub Actions → Docker builds.
    • Versioning: Semantic + Conventional Commits.

⸻

14. Milestones

Milestone Duration Deliverables
M0 – Setup Week 1 JHipster app scaffold, JWT, Liquibase, CI/CD
M1 – Market Mock Weeks 2–3 Mock data feed, tick WS, simple orders
M2 – Trading Core Weeks 4–5 Order flow, portfolio, ledger, margin check
M3 – Broker Portal Weeks 6–7 Client & funds management, EOD per broker
M4 – Exchange Console Week 8 Broker lifecycle, holidays, overrides
M5 – Kite Integration Weeks 9–10 Real Kite feed, live EOD, latency metrics
M6 – QA & Launch Week 11 Regression, load, pilot rollout

⸻

15. Definition of Done

✅ 100% acceptance per sprint
✅ All tests pass in CI
✅ Liquibase validated
✅ OpenAPI docs updated
✅ No P1 issues
✅ Full order-to-EOD verified

⸻

16. Risks & Mitigation

Risk Mitigation
Kite rate limits Mock feed until M5, throttle connections
Tick bursts WS buffer & broadcast throttling
Role complexity Strong RBAC & scoped queries
Settlement errors Automated EOD test suite

⸻

17. Success Metrics
    • 1,000 simulated traders online concurrently
    • <1% tick drop from feed
    • 100% ledger reconciliation accuracy
    • Complete EOD automation
    • Exchange overrides logged and reversible

⸻

✅ Summary:
RNExchange MVP delivers a realistic Indian multi-asset trading simulator with hierarchical governance (Exchange–Broker–Trader), real-time tick simulation, order matching, risk and margin control, and full EOD settlement—scalable from mock data to real Kite integration.
