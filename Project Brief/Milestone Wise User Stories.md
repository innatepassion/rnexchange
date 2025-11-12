📘 Milestone-Wise User Stories v1.2 — RNExchange

Document Version: 1.2
Aligned With: PRD v1.1 | UI/UX v1.1 | JHipster 8 (Spring Boot + React + WebSocket + PostgreSQL)
Auth: JWT
Date: November 2025

⸻

M0 – Foundations & Core Setup
Epic: Project scaffolding, environment setup, base modules.

Story ID User Story Acceptance Criteria Dependencies
M0-01 As a developer, I want to initialize the rnexchange JHipster monolith with JWT authentication so that base security and routing are in place. JWT login/register endpoints functional; default roles created (TRADER, BROKER_ADMIN, EXCHANGE_OP). None
M0-02 As a developer, I want database migration scripts so schema can evolve via Liquibase/Flyway. Schema versioned; migrations run automatically on startup. M0-01
M0-03 As a developer, I want entity generation via JDL for base domain (Users, Accounts, Orders, Instruments) so code is consistent. Entities compile with no constraint violations; mapstruct DTOs generated. M0-02
M0-04 As a QA engineer, I want unit tests scaffolding for services/repositories so CI passes on every push. Maven test suite executes successfully. M0-03
M0-05 As a developer, I want H2 (dev) and Postgres (prod) configs so local testing mirrors production. Profiles “dev” and “prod” selectable; data persists correctly. M0-02

⸻

M1 – Market Data Mock Engine
Epic: Create mock market-data subsystem for equities, futures, and commodities.

Story ID User Story Acceptance Criteria Dependencies
M1-01 As a trader, I want simulated live quotes via WebSocket so I can see dynamic prices. WS /marketdata emits ticks at 1 s interval using random-walk generator; reconnects on drop. M0
M1-02 As a developer, I want an instrument loader to seed BSE/NSE/MCX symbols. 1 000 + symbols inserted; verified unique per exchange. M1-01
M1-03 As a trader, I want watchlists to subscribe/unsubscribe to symbols. Add/remove persists in DB; updates real-time. M1-01
M1-04 As a QA engineer, I want deterministic replay of tick data for testing. Mock feed can replay from CSV using timestamped data. M1-01

⸻

M2 – Order Engine & Portfolio Accounting
Epic: Implement simulated trading, order state machine, ledger posting, and P&L.

Story ID User Story Acceptance Criteria Dependencies
M2-01 As a trader, I can place Market and Limit orders for cash equities. Orders transition NEW → ACCEPTED → FILLED; P&L updated. M1
M2-02 As a developer, I can persist executions and update positions. Execution table fills; Position qty = Σ fills. M2-01
M2-03 As a broker admin, I can view client orders and trades under my broker ID. Filtered list; RBAC enforced. M0
M2-04 As a system, I auto-journal ledger entries for trade cashflow. Debit/credit posted; balances reconcile. M2-02
M2-05 As a QA engineer, I verify pre/post-trade validations. Unit tests cover order rejection rules (invalid qty, closed market). M2-01

⸻

M3 – Back-Office Portal (Broker Admin)
Epic: Build broker portal for client management, funds, and risk view.

Story ID User Story Acceptance Criteria Dependencies
M3-01 As a broker admin, I can view traders linked to my broker. List filtered by broker_id; pagination enabled. M2
M3-02 As a broker admin, I can credit/debit funds through a journal form. Entry creates ledger record; trader balance updates instantly. M3-01
M3-03 As a broker admin, I can view risk and margin summary. Risk heatmap renders via WebSocket updates. M3-01
M3-04 As a broker admin, I can generate a trade blotter. CSV export + date filter; tested with > 10 000 rows. M2
M3-05 As a QA engineer, I can validate broker RBAC. Broker cannot access other traders; Exchange Op can view all. M0

⸻

M4 – Exchange Operator Console
Epic: Management and oversight features for exchange authority.

Story ID User Story Acceptance Criteria Dependencies
M4-01 As an exchange op, I can create/activate/deactivate brokers and traders. Status toggles reflect immediately; audit log updated. M3
M4-02 As an exchange op, I can define trading holidays and sessions. Calendar persisted; mock feed halts on holiday. M1
M4-03 As an exchange op, I can trigger daily settlement and override batches. EOD job recomputes MTM; overrides logged. M2
M4-04 As an exchange op, I can monitor system health. Dashboard shows feed latency < 500 ms and active sockets. M1

⸻

M5 – Kite API Integration
Epic: Replace mock feed with real-time market data from Zerodha Kite.

Story ID User Story Acceptance Criteria Dependencies
M5-01 As a developer, I can connect to Kite WebSocket feed. Live tick data ingested; 99 % uptime. M1
M5-02 As a system, I failover to mock feed if Kite disconnects. Auto-switch verified by integration tests. M5-01
M5-03 As a trader, I view live BSE/NSE/MCX quotes in app. LTP updates ≤ 300 ms latency. M5-01
M5-04 As a QA engineer, I validate Kite API keys and rate limits. No 429 errors; logs clean. M5-01

⸻

M6 – QA, Load, and Release
Epic: Stabilization, performance, and launch readiness.

Story ID User Story Acceptance Criteria Dependencies
M6-01 As a QA engineer, I run Gatling load tests on order endpoints. < 250 ms average latency @ 1 000 users. M2
M6-02 As a developer, I run Cucumber end-to-end flows. 100 % pass for place/cancel/order view. M0–M5
M6-03 As a product owner, I validate reports and statements. Daily/Monthly statement generated; CSV/PDF downloads. M3
M6-04 As a team, we deploy to staging with monitoring. Grafana dashboards active; alert rules defined. M0

⸻

🧭 Traceability Matrix

Milestone Primary Roles Deliverables Tests
M0 Dev / QA Base monolith + JWT Unit
M1 Trader / Dev Mock WS feed + Watchlists Integration
M2 Trader / Broker Trading + Ledger Unit + Cucumber
M3 Broker Admin Portal + Reports Cypress UI
M4 Exchange Op Console + Authority Integration
M5 Dev / Trader Kite WS integration Mock → Live switch
M6 QA / Ops Performance + Release Gatling
