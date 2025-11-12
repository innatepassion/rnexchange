MVP — Trading Simulator

1. Overview

A broker-grade paper trading platform for BSE/NSE cash and F&O plus MCX, built as a JHipster monolith (Spring Boot + React + WebSockets + PostgreSQL). It delivers real-time-feeling execution, P&L/MTM, simple margin checks, EOD settlement, and role-based back office—without real money. MVP runs a mock market data generator; Kite integration comes later. ￼

2. Roles & Scope (MVP)
   • Exchange Operator (super authority): create/activate/suspend brokers, manage trading calendar/holidays, run/override EOD settlements, see all data.
   • Broker Admin: manage only their clients; view blotter, client balances/MTM; post funds journals; start EOD for their scope.
   • Trader: place Market/Limit/Stop/Stop-Limit orders, manage watchlists, see portfolio, MTM & ledgers, download simple daily statement. ￼

3. Architecture (MVP)
   • Auth: JWT (built-in User/Authority), RBAC (TRADER, BROKER, EXCHANGE_OP, ADMIN).
   • Realtime: Spring WebSocket (STOMP); no Redis in MVP—broadcast directly from app node.
   • Data: PostgreSQL (prod), H2 (disk) in dev; Liquibase migrations.
   • Market data: in-process MockMarketService publishes ticks (LTP + OHLC) at 200–1000 ms; persists 1-min bars for charts & EOD marks.
   • No Kafka/Elasticsearch/Redis in MVP; add later when scaling and for analytics.
   • Testing: JUnit 5 + Mockito, Cucumber (BDD flows), Cypress (key E2E), Gatling (basic WS/REST load). ￼

4. Functional Modules (MVP)

A. Market Data (Mock)
• Symbol seeding (BSE/NSE/MCX subsets), trading sessions/holidays.
• Tick generation with basic volatility profile; 1-min bar builder.
• WS topics: /topic/quotes.{symbol}, /topic/bars.{symbol}.

B. Trading & Matching
• Order types: Market, Limit, Stop, Stop-Limit; TIF: DAY, IOC, GTC.
• Simple price-time book per symbol; configurable min latency and light slippage.
• Intraday square-off job at 15:20 IST (configurable). ￼

C. Portfolio, P&L, Margin
• Positions & lots (FIFO by default), realized/unrealized P&L, live MTM from ticks.
• Margin rules (simplified):
• Cash: 100% delivery; lower intraday per symbol tier.
• Futures/Options: per-contract initial/maintenance table (SPAN-lite).
• Breach detection + auto-liquidation policy (close riskiest first). ￼

D. Ledger & Settlement
• Journals: trade fees, variation margin, deposits/withdrawals.
• EOD batch: apply settlement prices → post MTM journals → generate HTML daily statements (PDF later).
• Exchange Operator can override/reverse a batch and re-run. ￼

E. Back Office UIs
• Trader: Market Watch, Order Ticket, Orders/Trades, Positions, Ledger, Statement download.
• Broker: Clients list (within broker), Funds Journal (credit/debit), Trade Blotter, Risk overview, EOD control.
• Exchange: Brokers (create/activate/suspend), Holidays, Settlement control (override/repost), System overview (basic health). ￼

5. Non-Functional (MVP)
   • Single node target; ~1,000 users feasible with WS fan-out in-process.
   • Observability: basic metrics/health via JHipster Admin UI; logs and request tracing minimal at first.
   • Security: short-lived JWT tokens; WS handshake validates JWT; strict role guards on endpoints. ￼

6. Out of Scope (MVP → later)
   • Kite integration (WS/REST) for live ticks (planned milestone).
   • Redis pub/sub, Kafka/Pulsar, Elasticsearch/Kibana dashboards.
   • Advanced corporate actions (beyond splits/dividends), options assignment detail reports, comprehensive PDF packs. ￼

7. Milestones (Revised)
   • M0 — Foundations: JHipster monolith scaffold; JWT; Liquibase; CI; H2 dev; Postgres prod.
   • M1 — Realtime Mock Market: Mock ticks + WS topics; 1-min bars; market watch UI skeleton.
   • M2 — Trading Core: Order ticket, matching, orders/fills; positions, MTM, ledgers; intraday square-off.
   • M3 — Broker Portal: clients, funds journal, blotter, risk snapshot; role scoping.
   • M4 — Exchange Console: brokers lifecycle, holidays/calendar, EOD runner with override.
   • M5 — Settlement & Reports: EOD MTM journals + HTML statements; (Begin) Kite adapter integration behind a feature flag.
   • M6 — QA & Launch: Cucumber scenarios, Cypress happy paths, Gatling smoke; hardening & docs. ￼
