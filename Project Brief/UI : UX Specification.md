RNExchange UI / UX Specification v1.1

Purpose

Define the user-experience, visual, and interaction guidelines for RNExchange — a multi-asset trading-simulator platform replicating brokerage workflows across Trader, Broker Admin, and Exchange Operator roles.

⸻

1 · Design Principles

Principle Description
Clarity Every screen must communicate instrument, balance, and order state instantly — no ambiguity.
Education & Transparency All data must carry the SIMULATED / DELAYED tag where relevant; tooltips explain training context.
Speed of Action Single-click trade, keyboard shortcuts, real-time socket updates.
Consistency Unified layout grid, typography, and component behavior across all roles.
Safety & Trust Explicit confirmations for order execution, cancellation, and settlements; clear error states.

⸻

2 · Visual Language

Element Specification
Typography Inter (Primary), Roboto Mono (Data / Numbers). Base 16 px grid, 1.5 rem line-height.
Color Tokens Implement via Tailwind CSS variables: --color-bull:#22C55E, --color-bear:#EF4444, --color-bg:#0F172A, --color-panel:#1E293B, --color-text:#E2E8F0.
Charts Lightweight line + candlestick (TradingView Lite or Recharts). SMA/EMA/RSI indicators.
Icons Lucide React pack; consistent 24 px stroke.
Feedback Toast for confirmations; non-blocking modals for edits.
Dark Mode Default Optimized for trading environments.

⸻

3 · Layout Framework

Role Landing Page Key Panels
Trader Market Watch / Dashboard Orders, Positions, Ledger, Option Chain, Watchlists
Broker Admin Broker Dashboard Clients, Funds Journal, Risk Monitor, Settlement Batch
Exchange Operator Exchange Overview Brokers, Holidays, Settlement Control, System Status

All share a 3-pane layout:
(A) Sidebar Nav → (B) Primary Work Area → (C) Context Panel (watchlist / filters).
Responsive down to 1024 px tablet. Drawer menu on mobile.

⸻

4 · Trader App

Screens & Features

Screen Functionality
Market Watch Add/remove symbols, subscribe to live ticks (WS), view bid-ask / LTP / % change, “SIMULATED” badge.
Order Ticket Market/Limit/Stop/Stop-Limit, TIF (DAY/IOC/GTC), quantity lot controls, margin preview, confirmation modal.
Orders & Trades Real-time update via WS; filter by status (New/Working/Filled/Cancelled); cancel/edit inline.
Positions Aggregated by symbol; unrealized + realized P&L; auto refresh; export CSV.
Ledger & Funds Deposits/withdrawals, fees, margin interest; search / sort.
Reports Daily statement (HTML → PDF later); contract note per day.
Watchlists Create multiple lists; drag-drop reorder; persist per user.

Interactions
• WebSocket connect/disconnect indicator.
• Quick Trade (shortcut T) opens ticket.
• Confirmations via toast + sound (optional).
• Empty states for new users.

⸻

5 · Broker Admin Portal

Screen Functionality
Dashboard Aggregate exposure, open orders, margin breaches, AUM (simulated).
Clients View only traders under this broker; create/activate/suspend.
Funds Journal Credit/debit; remarks / receipt upload (optional).
Trade Blotter Date / symbol filters; export CSV.
Risk Monitor Margin utilization heatmap; trigger auto square-off.
Settlement Batch Initiate EOD for broker; view status / re-run failed batches.

Interactions
• Role-based filter enforcement (brokerId scope).
• Confirmations for fund journals and batch operations.

⸻

6 · Exchange Console

Screen Functionality
Exchange Overview System stats, active brokers, traders online, mock feed status.
Broker Management Create, activate/deactivate brokers; reset credentials.
Holidays & Calendar Manage trading days / closures per exchange.
Settlement Control Global EOD run, override / reverse settlement, view logs.
Market Simulation Start/stop mock feed; view tick throughput.

Post-MVP Note:
Audit Explorer and cross-exchange reconciliation to be added in v2.

⸻

7 · Common Components

Component Behavior
Top Nav Breadcrumb, role switch, session timer, logout.
Tables Server-side pagination / sort; infinite scroll for live feeds.
Modals & Drawers Non-blocking; ESC / click-outside to dismiss.
Toasts Success (✓ green), Error (✕ red), Info (ℹ blue).
Skeleton Loaders Default for network calls > 500 ms.
Error Boundaries Display fallback w/ retry button.

⸻

8 · Accessibility & Localization
• WCAG 2.1 AA compliance.
• Color-contrast ≥ 4.5:1.
• Keyboard shortcuts for core actions (Tab, Enter, Esc, ↑/↓).
• Screen-reader labels for buttons and numeric fields.
• Language support: en, hi; dynamic switch in settings.

⸻

9 · Responsiveness

Device Layout
≥1280 px 3-column (trader terminal style).
1024–1279 px Collapsible sidebar, reduced data density.
<1024 px Drawer menu, vertical scroll cards for watchlists & orders.

⸻

10 · Form & Validation Guidelines

Type Pattern
Numeric Thousand separator auto-format; validate against tick size / lot size.
Date / Time 24-hour format (IST).
Error Messaging Human-readable: “Invalid price – must be multiple of ₹0.05”.
Confirmations Required for fund journal, square-off, settlement override.

⸻

11 · States & Feedback
• Loading: Skeleton rows + spinners.
• Empty: Illustration + CTA (“Start by adding symbols”).
• Error: Retry CTA + diagnostic message.
• Success: Toast + inline highlight.
• WS Disconnected: Banner “Realtime feed paused – reconnecting…”

⸻

12 · Deliverables

Artifact Owner Tool
Figma System Design Lead Figma → Dev Mode Tokens
Storybook Library Frontend Lead React + Tailwind
Interaction Prototypes UX Designer Figma Prototype
Icon Set Design Ops Lucide subset
UI Specs (per screen) Product Design PNG + Component Names
Responsive Specs Frontend Lead Breakpoint Preview in Storybook

Versioning: rnexchange-ui-kit v1.0.0 → increment minor on component additions.

⸻

13 · Implementation Notes
• Use React + TanStack Query for data fetch; subscribe WS for ticks.
• Form state with Formik + Yup.
• Component props strictly typed (TypeScript).
• All numerical data use monospace font for alignment.
• Theme and i18n through JHipster global context.

⸻

14 · Post-MVP Roadmap (Hints)

Feature Intent
Audit Explorer Exchange ops visibility / traceability.
Leaderboards & Challenges Gamified learning for traders.
Notifications Service In-app + push alerts via Redis/Kafka.
Analytics Dashboard ClickHouse + Recharts integration.

⸻

✅ Summary

This UI / UX spec ensures RNExchange delivers a realistic yet safe trading experience—fast, educational, and role-governed.
Every element is built for clarity, keyboard efficiency, and multi-device use, fully aligned with the MVP PRD and JHipster stack.
