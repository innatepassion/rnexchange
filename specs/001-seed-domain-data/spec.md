# Feature Specification: Seed Domain Trading Data Baseline

**Feature Branch**: `001-seed-domain-data`  
**Created**: 2025-11-13  
**Status**: Draft  
**Input**: User description: "For phase M0, the developer should treat it as a clean slate and set up a production-like, domain-aware baseline: first, drop the existing dev database or truncate all non-system tables so that all the generic JHipster sample data (Region, Country, etc.) and any test records are completely wiped, then re-run the app so Liquibase recreates the schema from the current JDL; next, create a dedicated Liquibase changelog (for example src/main/resources/config/liquibase/data/0001-seed-domain-data.xml) that seeds only meaningful trading data for this MVP, starting with three Exchange rows for NSE (National Stock Exchange, timezone Asia/Kolkata, status ACTIVE), BSE (Bombay Stock Exchange, Asia/Kolkata, ACTIVE), and MCX (Multi Commodity Exchange, Asia/Kolkata, ACTIVE), followed by a single \"demo\" broker like RN DEMO BROKING linked to NSE and marked ACTIVE, and at least one BrokerDesk user mapped to that broker and your existing JHipster User with ROLE_BROKER; then seed a small, curated instrument universe that reflects real trading context instead of placeholders, for example under NSE: RELIANCE, HDFCBANK, INFY, TCS, NIFTY50 (index spot proxy) with proper assetClass=EQUITY, currency=INR, realistic tickSize (0.05 or 0.01) and lotSize=1; under BSE: a couple of overlapping blue chips (e.g., RELIANCE_BSE, SBIN_BSE) to test multi-exchange symbol handling; under MCX: a handful of commodity futures underlyings like GOLD, CRUDEOIL, SILVER with assetClass=COMMODITY and larger tick/lot sizes; add a few Contract rows to represent F&O for the MVP, e.g. front-month NIFTY50 and BANKNIFTY index futures and one or two liquid equity futures (RELIANCE_FUT) with realistic expiries, plus at least one call and one put option contract each for NIFTY50 (optionType CE/PE, strikes near ATM); seed a basic MarketHoliday calendar entry for an upcoming trading holiday per exchange so the mock engine and UI can demonstrate halted trading behavior; then insert a couple of MarginRule records—for example a general NSE_CASH rule with initialPct=0.20, maintPct=0.15 and an NSE_FNO rule with initialPct=0.40, maintPct=0.30—so that pre-trade margin checks have something real to work with; finally, create one or two TraderProfile rows mapped to real JHipster User accounts (test trader logins), give each a TradingAccount linked to the demo broker with baseCcy=INR, balance seeded to something like 1000000 and status ACTIVE, and verify on startup that this domain-specific seed makes it possible to log in as admin, broker, and trader, see NSE/BSE/MCX instruments in the UI, and immediately place a simulated order on, say, RELIANCE at NSE using the mock market data without any leftover generic JHipster demo data polluting the experience."

## User Scenarios & Testing _(mandatory)_

### User Story 1 - Provision clean baseline (Priority: P1)

As a platform administrator preparing the MVP environment, I need to reset the trading database and load domain-specific baseline data so that all user roles experience production-like entities without legacy demo clutter.

**Why this priority**: Without a clean baseline and curated seed data, downstream user roles cannot exercise trading flows credibly.

**Independent Test**: Start a fresh environment, verify system bootstraps only the curated trading dataset, and confirm no generic demo records remain.

**Acceptance Scenarios**:

1. **Given** a fresh environment with no user data, **When** the platform bootstraps, **Then** only the curated exchanges, brokers, instruments, contracts, calendars, margin rules, and user profiles defined for the MVP are present.
2. **Given** an environment previously populated with demo entities, **When** the reset process runs, **Then** legacy entities are removed and replaced by the curated baseline set.

---

### User Story 2 - Broker workstation readiness (Priority: P2)

As a broker desk user assigned to the demo broker, I need to log in and see the active exchanges, mapped broker, and tradable instruments so I can support customer order entry immediately.

**Why this priority**: Broker operations must be ready to respond to traders on day one; lack of mapped data blocks fulfilment.

**Independent Test**: Log in using the broker role credential, verify the broker desk view shows the seeded broker, exchange memberships, and instrument catalog with correct attributes.

**Acceptance Scenarios**:

1. **Given** the broker desk user logs in, **When** they open the broker workspace, **Then** the RN DEMO BROKING profile is active and associated with NSE along with visible instrument metadata (asset class, tick size, lot size, currency).

---

### User Story 3 - Trader order simulation (Priority: P3)

As a trader using a seeded trading account, I want to place a simulated order on a seeded instrument (e.g., RELIANCE on NSE) so that I can validate core order lifecycle behaviour with realistic market context.

**Why this priority**: Trader experience validates the usefulness of the curated dataset and demonstrates MVP readiness for trading flows.

**Independent Test**: Log in as the trader profile, confirm account balance, and place a simulated order that completes normally using the seeded dataset.

**Acceptance Scenarios**:

1. **Given** the trader profile logs in with an active trading account, **When** they submit a buy order for a seeded instrument on its associated exchange, **Then** the order is accepted and processed using seeded market data without data validation errors.

---

### Edge Cases

- What happens when the seeding process is rerun on an environment already using the curated baseline (idempotent behaviour, duplicate prevention)?
- How does the system handle missing or inactive reference data during bootstrapping (e.g., exchange inactive, holiday date invalid)?
- How does the platform respond if user-role mappings referenced in the seed data are absent or renamed?

## Requirements _(mandatory)_

### Functional Requirements

- **FR-001**: The platform MUST provide an automated process to clear existing non-system data before applying the curated baseline dataset.
- **FR-002**: The platform MUST load three active exchanges representing NSE, BSE, and MCX with correct timezone metadata.
- **FR-003**: The platform MUST seed a single active broker entity (RN DEMO BROKING) linked to the NSE exchange and map an existing broker-role user to that broker desk.
- **FR-004**: The platform MUST seed instrument records for NSE, BSE, and MCX using realistic asset class, currency, tick size, and lot size attributes aligned with the MVP scope.
- **FR-005**: The platform MUST seed derivatives contract definitions covering index futures (NIFTY50, BANKNIFTY), a representative equity future (RELIANCE_FUT), and paired NIFTY50 call and put options with near-the-money strikes and upcoming expiries.
- **FR-006**: The platform MUST seed at least one upcoming market holiday per exchange to enable demonstration of halted trading behaviour.
- **FR-007**: The platform MUST seed margin rule configurations for NSE cash and derivatives segments with defined initial and maintenance margin percentages.
- **FR-008**: The platform MUST seed trader profiles mapped to real user accounts, each with an active trading account funded in INR and linked to the demo broker.
- **FR-009**: The platform MUST validate on startup that admin, broker, and trader roles can log in and access the seeded dataset without encountering references to removed demo content.

### Key Entities _(include if feature involves data)_

- **Exchange**: Represents a trading venue (NSE, BSE, MCX) including timezone, status, and relationships to brokers, instruments, contracts, and market holidays.
- **Broker**: Represents RN DEMO BROKING with exchange memberships and assigned broker desk users responsible for managing client orders.
- **Instrument**: Represents underlying tradable assets across exchanges (e.g., RELIANCE, NIFTY50, GOLD) with asset class, currency, tick size, and lot size attributes.
- **Contract**: Represents derivative instruments (index futures, equity futures, options) linked to underlying instruments, including expiry dates, option type, and strike as applicable.
- **MarketHoliday**: Represents exchange-specific non-trading days with date, description, and associated exchange.
- **MarginRule**: Represents pre-trade risk parameters per exchange segment specifying initial and maintenance margin percentages.
- **TraderProfile**: Represents trader identities linked to user accounts and associated trading accounts, permissions, and status.
- **TradingAccount**: Represents cash balances and account status for trader profiles, linked to brokers and denominated in a base currency.

## Assumptions & Dependencies

- Existing platform user accounts for admin, broker, and trader roles remain available to map against broker desks and trader profiles during seeding.
- The environment can be safely reset without impacting persistent integrations or downstream analytics that rely on previous demo data.
- The mock market data services already support the seeded instruments and contracts so simulated orders can reference matching symbols.
- Timezone configuration `Asia/Kolkata` is available within the platform configuration catalog for the NSE, BSE, and MCX exchanges.

## Success Criteria _(mandatory)_

### Measurable Outcomes

- **SC-001**: A freshly provisioned environment completes baseline seeding in under 5 minutes with zero generic demo records remaining.
- **SC-002**: Broker desk users can access the seeded broker and instrument catalog within 1 minute of login in 100% of smoke tests.
- **SC-003**: Trader users complete a simulated order submission on a seeded instrument without data validation errors in at least 3 consecutive end-to-end test runs.
- **SC-004**: Margin evaluation for seeded NSE cash and derivatives orders references the newly seeded margin rules in 100% of pre-trade checks during MVP testing.
