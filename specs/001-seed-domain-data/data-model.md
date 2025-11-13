# Data Model: Seed Domain Trading Data Baseline

## Overview

This feature introduces deterministic seed data for existing RNExchange domain entities. No new aggregates are created, but the seeded records must respect the following relationships and constraints to ensure idempotent Liquibase execution and realistic trading flows.

## Entity Snapshot (JDL-Aligned)

## Exchange

- **Purpose**: Authoritative list of trading venues (NSE, BSE, MCX).
- **Key fields**: `id` (Long, surrogate PK), `code` (String, unique, required), `name` (String, required), `timezone` (String, required — use `Asia/Kolkata`), `status` (`ExchangeStatus`, required — `ACTIVE`).
- **Relationships**: One-to-many via JPA to `Broker`, `Instrument`, `MarketHoliday`, `MarginRule`; `Contract` and `TradingAccount` link indirectly through instrument and broker relationships.
- **Validation**: Enforce uppercase `code`; ensure `timezone` matches supported config; `status` limited to enum values.

## Broker

- **Purpose**: Represents RN DEMO BROKING.
- **Key fields**: `code` (string, unique, required — e.g., `RN_DEMO`), `name` (string, required), `status` (string, required — use `ACTIVE`), `createdDate` (Instant).
- **Relationships**: Many-to-one with `Exchange`; one-to-many with `BrokerDesk` and `TradingAccount`.
- **Validation**: `status` limited to controlled vocabulary (`ACTIVE`, `SUSPENDED`, `TERMINATED`); exchange link required.

## BrokerDesk

- **Purpose**: Associates a JHipster `User` (ROLE_BROKER) with RN DEMO BROKING.
- **Key fields**: `name` (string, required).
- **Relationships**: Many-to-one with `Broker`; one-to-one with `User` via login.
- **Validation**: Ensure target `User` exists before seeding; enforce uniqueness per user.

## TraderProfile

- **Purpose**: Trader identities mapped to seeded trading accounts.
- **Key fields**: `displayName` (string, required), `email` (string, required), `mobile` (string, optional), `kycStatus` (`KycStatus`, required — use `APPROVED`), `status` (`AccountStatus`, required — `ACTIVE`).
- **Relationships**: One-to-one with `User`; one-to-many with `TradingAccount`.
- **Validation**: Email unique; must map to ROLE_TRADER `User` records; KYC status must be `APPROVED` for active accounts.

## TradingAccount

- **Purpose**: Funded accounts tied to trader and broker.
- **Key fields**: `type` (`AccountType`, required — `CASH`), `baseCcy` (`Currency`, required — `INR`), `balance` (BigDecimal, required, default `1000000`), `status` (`AccountStatus`, required — `ACTIVE`).
- **Relationships**: Many-to-one with `TraderProfile` and `Broker`.
- **Validation**: Balance ≥ 0; base currency must align with seeded instruments; enforce single active account per trader for MVP.

## Instrument

- **Purpose**: Cash-market instruments per exchange.
- **Key fields**: `symbol` (string, required, unique per exchange), `name` (string), `assetClass` (`AssetClass`, required — `EQUITY` for NSE/BSE symbols, `COMMODITY` for MCX), `exchangeCode` (string, required), `tickSize` (BigDecimal, required), `lotSize` (long, required), `currency` (`Currency`, required — `INR`), `status` (string, required — `ACTIVE`).
- **Relationships**: Many-to-one with `Exchange`; one-to-many with `Contract` and `DailySettlementPrice`.
- **Validation**: `tickSize` must divide price increments (0.05/0.01 for equities; 1.0+ for commodities); `lotSize` positive; `exchangeCode` references existing `Exchange.code`.

## Contract

- **Purpose**: Futures & options contracts attached to instruments.
- **Key fields**: `instrumentSymbol` (string, required), `contractType` (`ContractType`, required — `FUTURE` or `OPTION`), `expiry` (LocalDate, required), `strike` (BigDecimal, required for options), `optionType` (`OptionType`, required for options), `segment` (string, required — e.g., `NSE_FNO`).
- **Relationships**: Many-to-one with `Instrument`.
- **Validation**: Expiry must be >= current date; enforce `strike` null for futures; ensure unique `(instrumentSymbol, contractType, expiry, optionType, strike)` combination.

## MarketHoliday

- **Purpose**: Future non-trading dates per exchange.
- **Key fields**: `tradeDate` (LocalDate, required), `reason` (string), `isHoliday` (boolean, required — set `true`).
- **Relationships**: Many-to-one with `Exchange`.
- **Validation**: `tradeDate` must be future-dated relative to seed execution; unique per `Exchange` + `tradeDate`.

## MarginRule

- **Purpose**: Pre-trade margin configuration.
- **Key fields**: `scope` (string, required — e.g., `NSE_CASH`, `NSE_FNO`), `initialPct` (BigDecimal), `maintPct` (BigDecimal), `spanJson` (TextBlob, optional).
- **Relationships**: Many-to-one with `Exchange`.
- **Validation**: Percentages expressed as decimals (`0.20`); `initialPct` ≥ `maintPct`; enforce unique `scope` per exchange.

## User Mappings

- Leverage existing `user.csv` & `user_authority.csv` (admin, broker, trader). Ensure broker desk user retains `ROLE_BROKER`; trader user retains `ROLE_TRADER`.
- Validate that seeded `BrokerDesk` and `TraderProfile` link to these user IDs; add new trader if necessary via Liquibase changeset.

## Relationship Summary

- `Exchange` → `Broker` → `BrokerDesk`/`TradingAccount` (organizational hierarchy).
- `TraderProfile` ↔ `User` (one-to-one) and `TraderProfile` → `TradingAccount` (one-to-many).
- `Exchange` → `Instrument` → `Contract` (market structure).
- `Exchange` → `MarketHoliday`, `Exchange` → `MarginRule` (compliance/risk).
- Referential integrity enforced via Liquibase foreign keys; seeding must insert parent entities before dependents.

## State & Lifecycle Notes

- `Exchange.status`: Seed as `ACTIVE`. Future toggles to `INACTIVE` handled via admin workflows (out-of-scope).
- `Broker.status` & `TradingAccount.status`: Start `ACTIVE`; transitions to `SUSPENDED` or `INACTIVE` controlled by risk services; maintain event hooks for audit (no direct changes in seed).
- Margin rules and holidays considered immutable in baseline; updates occur via later features using Liquibase incremental changes.
