📘 Screen by Screen Functional Specification (v1.2)

For MVP — rnExchange Trading Simulator Platform

Roles: Trader • Broker Admin • Exchange Operator
Auth: JWT
Realtime: WebSockets (mock feed → Kite integration in M5)
Cache: None (direct DB reads; time-series DB optimized)
Stack: React (frontend), Spring Boot (backend, JHipster v8), PostgreSQL, H2 (dev)

⸻

🧩 1. TRADER APP

1.1 Login / Signup

Purpose: User authentication and onboarding for traders.
Features:
• JWT-based authentication (/auth/login, /auth/signup).
• Email/password validation.
• Accept Terms of Use and simulate KYC (no actual data stored).
• Persistent session with refresh token rotation.
Validations: Email format, password policy, duplicate email check.
Dependencies: Auth API, User entity.

⸻

1.2 Dashboard

Purpose: Show trader’s summary view of current performance.
Features:
• Portfolio value (MTM), cash balance, and P&L summary.
• Quick view of open positions, orders, and today’s gainers/losers.
• Quick access tiles: Trade, Watchlist, Ledger, Reports.
• Leaderboard rank and challenge badges.
Real-time: Update every 2–5 seconds via WS (portfolio and prices).
Dependencies: Positions, Orders, MarketData, Leaderboard services.

⸻

1.3 Market Watch

Purpose: Monitor price updates and instrument data.
Features:
• Watchlist creation & management.
• Live ticker stream (mock → Kite later).
• Search instrument by symbol or name.
• Add/remove instruments to watchlists.
Real-time: Live price & volume updates.
Validations: Max 50 symbols per list.
Dependencies: MarketData, Instruments.

⸻

1.4 Order Ticket

Purpose: Execute trades (buy/sell).
Features:
• Market, Limit, Stop, Stop-Limit order types.
• Quantity, Price, TIF (DAY, IOC, GTC).
• Order preview with margin impact & estimated fees.
• Confirmation modal before submit.
• Displays rejection if insufficient balance or market closed.
Validations: Numeric input, tick-size multiples, trading hours.
Real-time: Shows best bid/ask, simulated slippage, and fill confirmation.
Dependencies: Orders, MarketData, Risk/Margin services.

⸻

1.5 Orders & Trades

Purpose: Manage and track all orders and fills.
Features:
• Filter by date, symbol, status (new, filled, canceled).
• Cancel or modify open orders.
• View fills and trade details.
Real-time: WS-based order status push.
Dependencies: Orders, Executions.

⸻

1.6 Portfolio

Purpose: Show holdings, unrealized/realized P&L, and metrics.
Features:
• Positions table: qty, avg cost, last price, MTM, unrealized P&L.
• Aggregated metrics: total exposure, margin used.
• Drilldown to lot details (FIFO/LIFO).
• Graphical performance chart (daily equity curve).
Dependencies: Portfolio, MarketData, Risk.

⸻

1.7 Ledger

Purpose: Track all journal entries and cash movements.
Features:
• List of all debits/credits (trades, dividends, interest, etc.).
• Filter by type/date.
• Export CSV.
Dependencies: LedgerEntry, Reports services.

⸻

1.8 Reports

Purpose: Historical reports for compliance-like transparency.
Features:
• P&L Statement, Contract Note (PDF/HTML), Trade Blotter.
• Date-range picker, export options.
Dependencies: Reports, Ledger, Executions.

⸻

1.9 Challenges & Leaderboards

Purpose: Gamify learning & practice trading.
Features:
• Join public leagues or private challenges.
• View leaderboard by return %, Sharpe, or win rate.
• “Follow” ghost trades of top users (paper only).
Dependencies: Leaderboard, Analytics.

⸻

🧮 2. BROKER ADMIN APP

2.1 Login

Purpose: Secure access for broker operations staff.
Features:
• JWT-based broker login (role = BROKER_ADMIN).
• Password reset and MFA (optional).
Dependencies: Auth API.

⸻

2.2 Dashboard

Purpose: Overview of all clients & activity under the broker.
Features:
• Client AUM, active/inactive users, order volumes.
• Risk and margin exposure summary.
• Graphs for trade count, volume, and revenue.
Dependencies: Accounts, Orders, Risk, Ledger.

⸻

2.3 Clients

Purpose: Manage trader accounts.
Features:
• List of all traders under this broker.
• View details: balance, margin, open positions, P&L.
• Activate/deactivate account.
• Reset password or assign promotions.
Validations: Only broker-owned clients accessible.
Dependencies: Accounts, User, Ledger, Risk.

⸻

2.4 Funds Journal

Purpose: Simulate deposit/withdrawal actions.
Features:
• Credit/debit balance (journal entry).
• Add memo & reason (promotion, margin call, etc.).
• Reflects in trader’s ledger instantly.
Dependencies: JournalEntry, Ledger.

⸻

2.5 Risk & Margin View

Purpose: Monitor risk exposure for all traders.
Features:
• Margin utilization, leverage ratios, auto-liq queue.
• Trigger manual liquidation for user.
• Export margin summary CSV.
Dependencies: RiskService, Orders, Positions.

⸻

2.6 Reports

Purpose: Broker-specific analytics and statements.
Features:
• Trade blotter, settlement summary, EOD P&L.
• Custom date-range filters, PDF export.
Dependencies: Reports, Ledger, Accounts.

⸻

🏛️ 3. EXCHANGE OPERATOR APP

3.1 Exchange Dashboard

Purpose: Control center for all brokers, users, and market data.
Features:
• Total users, brokers, AUM, open positions, system alerts.
• Status widgets for feeds (mock generator, Kite API, EOD batch).
Dependencies: MarketData, Accounts, SystemHealth.

⸻

3.2 Broker Management

Purpose: Manage broker accounts.
Features:
• Create, edit, activate/deactivate brokers.
• Assign brokers to traders.
• View broker summary (clients, AUM, volume).
Dependencies: BrokerDesk, User.

⸻

3.3 Trader Oversight

Purpose: Full visibility across all users.
Features:
• View all trader accounts (across brokers).
• Force deactivate/reactivate.
• Impersonate (read-only) for support.
Dependencies: Accounts, User, AuditLog.

⸻

3.4 Market Data Control

Purpose: Manage data ingestion & feed integrity.
Features:
• Start/stop mock data generator.
• Configure feed latency, frequency, and session times.
• Manage trading holidays and sessions.
Dependencies: MarketData, ExchangeConfig.

⸻

3.5 Settlement & Override

Purpose: Manage daily settlements, overrides, and rollbacks.
Features:
• Trigger manual EOD batch: MTM, variation margin, statement generation.
• Override a specific trade or position (admin authority).
• Audit logs of all overrides.
Dependencies: SettlementBatch, Risk, Ledger, AuditLog.

⸻

3.6 Corporate Actions

Purpose: Manage splits, dividends, and symbol changes.
Features:
• Schedule and apply corporate actions.
• Preview before apply.
• Auto-update affected accounts & prices.
Dependencies: CorporateAction, Portfolio.

⸻

🔄 4. SHARED COMPONENTS

Component Description Services Real-Time
Auth & Role Management JWT-based login, access control per role Auth, Users No
WebSocket Layer Market data, orders, positions updates MarketData, Orders Yes
Notifications Alerts, fills, warnings NotificationService Optional
Reports Engine PDF/HTML via OpenPDF/Flying Saucer Reports, Ledger No
Audit Log Records every admin/trader/broker action AuditLog Yes (stream)
Theme / UI React + Tailwind, responsive layout UI library No

⸻

⚙️ Developer Notes
• Phase-1 (M1): Mock data generator for quotes & trades.
• Phase-2 (M5): Kite API integration for live market feed.
• Cache: None; direct fetch from PostgreSQL or time-series DB (ClickHouse optional).
• Testing: JUnit + Cucumber (backend), Cypress (UI), Gatling (perf).
• Build Tools: Maven + Spring Boot (JHipster v8).
