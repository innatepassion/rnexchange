# RNExchange — Multi-Asset Trading Simulator Platform

[![JHipster 8.11.0](https://img.shields.io/badge/JHipster-8.11.0-blue.svg)](https://www.jhipster.tech)
[![Constitution v1.0.0](https://img.shields.io/badge/Constitution-v1.0.0-green.svg)](.specify/memory/constitution.md)

> **A broker-grade paper trading platform for BSE/NSE cash and F&O plus MCX commodities**  
> Built as a JHipster monolith (Spring Boot + React + WebSockets + PostgreSQL) to deliver real-time execution, P&L/MTM tracking, margin management, EOD settlement, and role-based back office—without real money.

---

## 🎯 Project Overview

**RNExchange** is a full-fledged multi-segment trading and brokerage simulation ecosystem designed to mirror the operations of a real-world securities broker. It delivers a unified environment where traders, brokers, and exchange operators can interact over simulated but realistic market conditions for Indian financial markets.

### Key Features

- 🎓 **Educational First**: Transparent simulation environment with "SIMULATED" badges and learning-focused UX
- 📊 **Real-Time Trading**: WebSocket-powered live market data, order updates, and portfolio tracking
- 👥 **Multi-Role Governance**: Exchange Operator → Broker Admin → Trader hierarchy with strict RBAC
- 💰 **Complete Lifecycle**: Order placement → Execution → Position tracking → P&L → Settlement
- 🛡️ **Risk Management**: Margin rules, breach detection, auto-liquidation policies
- 📈 **Market Segments**: BSE/NSE cash equities, F&O derivatives, MCX commodities
- 🔄 **Settlement Engine**: EOD batch processing, variation margin, statement generation

---

## 🏗️ Architecture

### Tech Stack

| Layer          | Technology                                   |
| -------------- | -------------------------------------------- |
| **Frontend**   | React 18 + TypeScript + Redux Toolkit        |
| **Backend**    | Spring Boot 3.x + Spring WebSocket (STOMP)   |
| **Database**   | PostgreSQL (prod), H2 Disk (dev)             |
| **Auth**       | JWT with role-based access control           |
| **Real-Time**  | Spring WebSocket + STOMP protocol            |
| **API**        | REST + OpenAPI 3.0 (API-first development)   |
| **Testing**    | JUnit 5, Mockito, Cucumber, Cypress, Gatling |
| **Build**      | Maven + Webpack                              |
| **Deployment** | Docker + Docker Compose                      |

### Domain Model (20 Entities)

```
Organization:     Exchange, Broker, BrokerDesk, ExchangeOperator, ExchangeIntegration, MarketHoliday
User Profiles:    TraderProfile, TradingAccount
Market Data:      Instrument, Contract, DailySettlementPrice
Trading:          Order, Execution, Position, Lot
Accounting:       LedgerEntry
Risk:             MarginRule, RiskAlert
Settlement:       SettlementBatch, CorporateAction
```

All entities are defined in [`rnexchange.jdl`](rnexchange.jdl) and follow Domain-Driven Design principles.

---

## 👥 User Roles & Access

### 🏛️ Exchange Operator (Super Authority)

- Manage brokers (create, activate, suspend)
- Control trading calendar and holidays
- Run/override EOD settlements
- System-wide visibility and control

### 🏢 Broker Admin

- Manage traders under their broker
- View trade blotter and client balances
- Post fund journals (deposits/withdrawals)
- Monitor risk and margin utilization
- Initiate EOD for broker scope

### 📈 Trader

- Place orders (Market, Limit, Stop, Stop-Limit)
- Manage watchlists and portfolios
- View positions, MTM, and P&L
- Access ledger and download statements

---

## 🚀 Quick Start

### Prerequisites

- **Java 21** or later
- **Node.js 20.x** or later
- **PostgreSQL 15+** (for production) or use H2 (for development)
- **Maven 3.9+** (or use included `./mvnw`)

### Development Setup

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd rnexchange
   ```

2. **Install dependencies**
   ```bash
   ./mvnw clean install
   ./npmw install
   ```

````

3. **Start PostgreSQL** (or skip for H2)
   ```bash
   docker compose -f src/main/docker/postgresql.yml up -d
````

4. **Run the application**

   Terminal 1 (Backend):

   ```bash
   ./mvnw spring-boot:run
   ```

````

   Terminal 2 (Frontend dev server):
   ```bash
./npmw start
````

5. **Access the application**
   - Application: http://localhost:9000
   - API Docs: http://localhost:8080/swagger-ui/
   - Default admin: `admin` / `admin`

### Using H2 Database (Development)

The application uses H2 disk-based database by default in `dev` profile:

- Console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:file:./target/h2db/db/rnexchange`
- Username: `rnexchange`

---

## 📖 Project Documentation

### Core Documents

- **[Constitution](.specify/memory/constitution.md)** — Project principles and governance rules
- **[JDL Schema](rnexchange.jdl)** — Domain model definition
- **[OpenAPI Spec](src/main/resources/swagger/api.yml)** — REST API contracts
- **[Project Brief](Project%20Brief/)** — MVP requirements, user stories, UI/UX specs

### Key Principles (Constitution v1.0.0)

1. ✅ **Test-Driven Development** — NON-NEGOTIABLE (Red-Green-Refactor cycle)
2. 🏗️ **JHipster Conventions** — Follow JHipster best practices strictly
3. 🔒 **Role-Based Governance** — Three-tier RBAC enforced at all layers
4. ⚡ **Real-Time Architecture** — WebSocket for market data and order updates
5. 🎓 **Educational Transparency** — Clear "SIMULATED" indicators everywhere
6. 🧠 **Domain-Driven Design** — Rich domain model with business logic in services
7. 📋 **API-First Development** — OpenAPI specs before implementation

---

## 🧪 Testing

### Run All Tests

```bash
./mvnw verify                    # Backend tests
./npmw test                      # Frontend tests
```

### Test Categories

**Backend Tests** (JUnit 5 + Mockito)

```bash
./mvnw test                      # Unit + integration tests
./mvnw test -Dtest=*IT           # Integration tests only
```

**BDD Tests** (Cucumber)

```bash
./mvnw test -Dtest=CucumberIT
```

**E2E Tests** (Cypress)

```bash
./mvnw spring-boot:run           # Terminal 1
./npmw run e2e                   # Terminal 2
```

**Performance Tests** (Gatling)

```bash
./mvnw gatling:test              # Load test all entities
```

**Code Quality** (SonarQube)

```bash
docker compose -f src/main/docker/sonar.yml up -d
./mvnw -Pprod clean verify sonar:sonar
```

Access SonarQube at http://localhost:9001

---

## 🏗️ Development Workflow

### API-First Development

1. **Define API contract** in `src/main/resources/swagger/api.yml`
2. **Generate code** from OpenAPI spec:
   ```bash
   ./mvnw generate-sources
   ```
3. **Implement delegate classes** with `@Service` annotations
4. **Write tests** (TDD: Red → Green → Refactor)
5. **Verify** via Swagger UI

### Adding/Modifying Entities

1. **Edit JDL** file: `rnexchange.jdl`
2. **Regenerate entities**:
   ```bash
   jhipster jdl rnexchange.jdl
   ```
3. **Review generated files**:
   - Backend: `domain/`, `repository/`, `service/`, `web/rest/`
   - Frontend: `webapp/app/entities/`
   - Database: `resources/config/liquibase/changelog/`
4. **Add custom business logic** in service classes
5. **Write tests** and verify

### Working with WebSockets

WebSocket topics follow this pattern:

- Market data: `/topic/quotes.{symbol}`
- Order updates: `/topic/orders.{userId}`
- Portfolio MTM: `/topic/portfolio.{accountId}`

See `WebsocketConfiguration.java` and `websocket-middleware.ts` for implementation.

---

## 📦 Building for Production

### Package as JAR

```bash
./mvnw -Pprod clean verify
java -jar target/*.jar
```

### Package as WAR

```bash
./mvnw -Pprod,war clean verify
```

### Docker Build

```bash
npm run java:docker                    # Standard build
npm run java:docker:arm64              # ARM64 (M1/M2 Macs)
```

### Full Docker Deployment

```bash
docker compose -f src/main/docker/app.yml up -d
```

---

## 🐳 Docker Services

| Service                     | Command                                                               | URL                            |
| --------------------------- | --------------------------------------------------------------------- | ------------------------------ |
| **PostgreSQL**              | `docker compose -f src/main/docker/postgresql.yml up -d`              | localhost:5432                 |
| **JHipster Control Center** | `docker compose -f src/main/docker/jhipster-control-center.yml up -d` | http://localhost:7419          |
| **SonarQube**               | `docker compose -f src/main/docker/sonar.yml up -d`                   | http://localhost:9001          |
| **Monitoring Stack**        | `docker compose -f src/main/docker/monitoring.yml up -d`              | Grafana: http://localhost:3000 |
| **Swagger Editor**          | `docker compose -f src/main/docker/swagger-editor.yml up -d`          | http://localhost:7742          |

---

## 📅 Development Milestones

| Milestone                 | Status          | Description                                                    |
| ------------------------- | --------------- | -------------------------------------------------------------- |
| **M0 — Foundations**      | ✅ **COMPLETE** | JHipster scaffold, JWT, Liquibase, 20 entities, CI/CD setup    |
| **M1 — Market Data Mock** | 🚧 Next         | Mock tick generator, WebSocket topics, 1-min bars, watchlists  |
| **M2 — Trading Core**     | 📋 Planned      | Order matching, positions, P&L, ledger, margin checks          |
| **M3 — Broker Portal**    | 📋 Planned      | Client management, funds journal, risk monitor, EOD per broker |
| **M4 — Exchange Console** | 📋 Planned      | Broker lifecycle, holidays, settlement overrides               |
| **M5 — Kite Integration** | 📋 Planned      | Real Zerodha Kite feed, live EOD, latency metrics              |
| **M6 — QA & Launch**      | 📋 Planned      | Regression, load testing, pilot rollout                        |

**Current Focus**: M1 (Market Data Mock Engine)

---

## 🔧 Project Structure

```
rnexchange/
├── src/main/
│   ├── java/com/rnexchange/          # Backend (Spring Boot)
│   │   ├── domain/                    # JPA entities (20 entities)
│   │   ├── repository/                # Spring Data JPA repositories
│   │   ├── service/                   # Business logic layer
│   │   ├── web/rest/                  # REST API controllers
│   │   ├── web/websocket/             # WebSocket endpoints
│   │   ├── security/                  # JWT & RBAC configuration
│   │   └── config/                    # Spring configuration
│   │
│   ├── resources/
│   │   ├── config/
│   │   │   ├── application*.yml       # Environment configs
│   │   │   └── liquibase/             # Database migrations
│   │   └── swagger/api.yml            # OpenAPI 3.0 specification
│   │
│   ├── webapp/app/                    # Frontend (React + TypeScript)
│   │   ├── entities/                  # CRUD UIs for 20 entities
│   │   ├── modules/                   # Feature modules (home, login, admin)
│   │   ├── shared/                    # Shared components & utilities
│   │   └── config/                    # Redux store, WebSocket, i18n
│   │
│   └── docker/                        # Docker Compose configurations
│
├── src/test/                          # Test suite
│   ├── java/                          # JUnit 5, Mockito, Cucumber
│   ├── javascript/cypress/            # Cypress E2E tests
│   └── gatling/                       # Gatling performance tests
│
├── .specify/                          # Project governance
│   ├── memory/constitution.md         # Project constitution v1.0.0
│   └── templates/                     # Feature spec/plan/task templates
│
├── Project Brief/                     # Requirements & specifications
├── .jhipster/                         # Entity metadata (JSON)
├── rnexchange.jdl                     # JHipster Domain Language definition
└── README.md                          # This file
```

---

## 🌍 Internationalization

Supported languages:

- 🇬🇧 English (`en`) — Default
- 🇮🇳 Hindi (`hi`)

Translation files: `src/main/webapp/i18n/{lang}/` and `src/main/resources/i18n/messages_{lang}.properties`

---

## 🤝 Contributing

### Development Guidelines

1. **Follow the Constitution** — All code must comply with [project principles](.specify/memory/constitution.md)
2. **TDD is Mandatory** — Write failing tests before implementation
3. **API-First** — Update OpenAPI spec before coding endpoints
4. **Conventional Commits** — Use `feat:`, `fix:`, `docs:`, `test:`, `refactor:`
5. **Code Reviews** — All PRs require approval
6. **Test Coverage** — Maintain 90% backend, 80% frontend coverage

### Feature Development Process

1. Create feature spec in `/specs/[###-feature-name]/spec.md`
2. Generate implementation plan via `/speckit.plan` command
3. Write failing tests (contract, integration, unit)
4. Implement minimum code to pass tests
5. Refactor for clarity and maintainability
6. Submit PR with tests, implementation, and updated docs

### Branching Convention

- Feature branches: `[###-feature-name]` (e.g., `001-market-watch`, `002-order-ticket`)
- Keep commits small and atomic
- No force-push to `main` branch

---

## 📊 Performance Targets

| Metric                         | Target      | Status               |
| ------------------------------ | ----------- | -------------------- |
| Concurrent Users               | 1,000+      | To be validated (M6) |
| Order Latency (p95)            | <250 ms     | To be validated (M2) |
| WebSocket Updates/sec          | 10,000 peak | To be validated (M1) |
| EOD Settlement (10k positions) | <5 minutes  | To be validated (M3) |
| Test Coverage (Backend)        | ≥90%        | ✅ Current           |
| Test Coverage (Frontend)       | ≥80%        | ✅ Current           |

---

## 📚 Additional Resources

### JHipster Documentation

- [JHipster 8.11.0 Documentation](https://www.jhipster.tech/documentation-archive/v8.11.0)
- [Using JHipster in Development](https://www.jhipster.tech/documentation-archive/v8.11.0/development/)
- [Using JHipster in Production](https://www.jhipster.tech/documentation-archive/v8.11.0/production/)
- [Doing API-First Development](https://www.jhipster.tech/documentation-archive/v8.11.0/doing-api-first-development/)

### Technologies

- [Spring Boot 3.x](https://spring.io/projects/spring-boot)
- [React 18](https://react.dev/)
- [Redux Toolkit](https://redux-toolkit.js.org/)
- [Spring WebSocket](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [PostgreSQL](https://www.postgresql.org/)
- [Liquibase](https://www.liquibase.org/)

---

## 🛠️ Command-Line Tools & Scripts

### Maven Wrapper (`./mvnw`)

The Maven wrapper ensures consistent Maven version across all environments.

```bash
# Clean and compile
./mvnw clean compile

# Run tests
./mvnw test                              # Unit tests only
./mvnw verify                            # Unit + integration tests
./mvnw test -Dtest=OrderServiceIT        # Specific test class

# Run application
./mvnw spring-boot:run                   # Dev mode with hot reload
./mvnw -Pprod spring-boot:run            # Production profile

# Generate sources from OpenAPI
./mvnw generate-sources

# Database migrations
./mvnw liquibase:update                  # Apply pending migrations
./mvnw liquibase:rollback -Dliquibase.rollbackCount=1

# Code quality
./mvnw checkstyle:check                  # Java style check
./mvnw spotless:check                    # Code formatting check
./mvnw spotless:apply                    # Auto-format code

# Performance tests
./mvnw gatling:test                      # Run all Gatling scenarios
./mvnw gatling:test -Dgatling.simulationClass=OrderGatlingTest

# Build & package
./mvnw clean package                     # Build JAR
./mvnw -Pprod clean verify               # Production build with optimization
./mvnw -Pprod,war clean verify           # Build WAR for app servers

# Docker image
./mvnw -Pprod clean verify jib:dockerBuild
```

### NPM Wrapper (`./npmw`)

The NPM wrapper ensures Node.js and npm are locally installed and versioned correctly.

```bash
# Install dependencies
./npmw install                           # Install all dependencies
./npmw install --save <package>          # Add runtime dependency
./npmw install --save-dev <package>      # Add dev dependency

# Development
./npmw start                             # Start webpack dev server (port 9000)
./npmw run start                         # Same as above

# Testing
./npmw test                              # Run Jest unit tests
./npmw run test:watch                    # Jest in watch mode
./npmw run e2e                           # Cypress E2E tests
./npmw run e2e:headless                  # Cypress headless mode
./npmw run e2e:cypress:audits            # Lighthouse audits

# Code quality
./npmw run lint                          # ESLint check
./npmw run lint:fix                      # Auto-fix ESLint issues
./npmw run prettier:check                # Check code formatting
./npmw run prettier:format               # Auto-format code

# Build
./npmw run build                         # Production build
./npmw run webpack:build:main            # Build main bundle
./npmw run webapp:build:dev              # Development build
./npmw run webapp:build:prod             # Production build with optimization

# Docker
npm run java:docker                      # Build Docker image (x86_64)
npm run java:docker:arm64                # Build Docker image (ARM64)
```

### JHipster CLI Commands

```bash
# Entity management
jhipster entity <EntityName>             # Generate new entity interactively
jhipster jdl rnexchange.jdl              # Import/update entities from JDL
jhipster jdl rnexchange.jdl --skip-install  # Skip npm/maven install

# Code generation
jhipster spring-controller <name>        # Generate REST controller
jhipster spring-service <name>           # Generate service class

# CI/CD setup
jhipster ci-cd                           # Generate CI/CD configs

# Language support
jhipster languages                       # Add/remove languages

# Database
jhipster database-changelog              # Create incremental changelog

# Upgrade
jhipster upgrade                         # Upgrade JHipster version

# Information
jhipster info                            # Display project info
```

### Database Management

```bash
# PostgreSQL (Docker)
docker compose -f src/main/docker/postgresql.yml up -d     # Start
docker compose -f src/main/docker/postgresql.yml down      # Stop
docker compose -f src/main/docker/postgresql.yml logs -f   # View logs

# Connect to PostgreSQL
psql -h localhost -U rnexchange -d rnexchange

# H2 Console (dev mode)
# Access at: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:file:./target/h2db/db/rnexchange
# Username: rnexchange
# Password: (empty)

# Liquibase operations
./mvnw liquibase:status                  # Check migration status
./mvnw liquibase:diff                    # Generate diff changelog
./mvnw liquibase:clearCheckSums          # Clear checksums
```

### Git Workflow Commands

```bash
# Start new feature
git checkout -b 001-market-data-mock

# Commit with conventional commits
git commit -m "feat(market-data): add mock tick generator service"
git commit -m "fix(orders): resolve position calculation bug"
git commit -m "test(trading): add order matching integration tests"
git commit -m "docs(readme): update quickstart guide"
git commit -m "refactor(services): extract margin calculation logic"

# Pre-push validation
./mvnw verify                            # Run all backend tests
./npmw test                              # Run all frontend tests
./mvnw checkstyle:check                  # Check code style
./npmw run lint                          # Check JS/TS style

# Update from main
git fetch origin
git rebase origin/main

# Interactive rebase for clean history
git rebase -i HEAD~3
```

### Docker Compose Shortcuts

```bash
# Start all services
docker compose -f src/main/docker/services.yml up -d

# Individual services
docker compose -f src/main/docker/postgresql.yml up -d
docker compose -f src/main/docker/jhipster-control-center.yml up -d
docker compose -f src/main/docker/sonar.yml up -d
docker compose -f src/main/docker/monitoring.yml up -d

# View logs
docker compose -f src/main/docker/services.yml logs -f

# Stop and cleanup
docker compose -f src/main/docker/services.yml down
docker compose -f src/main/docker/services.yml down -v  # Remove volumes too

# Full app deployment
docker compose -f src/main/docker/app.yml up -d
docker compose -f src/main/docker/app.yml logs -f rnexchange-app
```

---

## 🗂️ Detailed Project Structure

### Backend Java Structure

```
src/main/java/com/rnexchange/
│
├── 🎯 Application Entry Point
│   ├── RnexchangeApp.java                    # Spring Boot @SpringBootApplication
│   └── ApplicationWebXml.java                # WAR deployment configuration
│
├── 🏗️ Domain Layer (JPA Entities)
│   └── domain/
│       ├── AbstractAuditingEntity.java       # Base class with created/modified audit
│       ├── User.java                         # Built-in user entity (JWT)
│       ├── Authority.java                    # User roles/authorities
│       │
│       ├── Exchange.java                     # Root organization entity
│       ├── Broker.java                       # Broker under exchange
│       ├── BrokerDesk.java                   # Broker desk/team
│       ├── ExchangeOperator.java             # Exchange operator profile
│       ├── ExchangeIntegration.java          # External feed integration (Kite)
│       ├── MarketHoliday.java                # Trading calendar holidays
│       │
│       ├── TraderProfile.java                # Trader user profile
│       ├── TradingAccount.java               # Trading account with balance
│       │
│       ├── Instrument.java                   # Base instrument (stocks, commodities)
│       ├── Contract.java                     # Derivatives contract (F&O)
│       ├── DailySettlementPrice.java         # EOD settlement prices
│       │
│       ├── Order.java                        # Order entity
│       ├── Execution.java                    # Order execution/fill
│       ├── Position.java                     # Aggregate position per instrument
│       ├── Lot.java                          # Position lots (FIFO tracking)
│       │
│       ├── LedgerEntry.java                  # Double-entry accounting
│       │
│       ├── MarginRule.java                   # Margin requirements per segment
│       ├── RiskAlert.java                    # Risk breach alerts
│       │
│       ├── CorporateAction.java              # Splits, dividends, etc.
│       ├── SettlementBatch.java              # EOD settlement batch
│       │
│       └── enumeration/                      # Enums (17 total)
│           ├── AccountStatus.java            # ACTIVE, INACTIVE, SUSPENDED
│           ├── AccountType.java              # CASH, MARGIN
│           ├── AlertType.java                # MARGIN_BREACH, AUTO_SQOFF, etc.
│           ├── AssetClass.java               # EQUITY, FUTURE, OPTION, COMMODITY
│           ├── ContractType.java             # FUTURE, OPTION
│           ├── CorporateActionType.java      # SPLIT, DIVIDEND, MERGER, etc.
│           ├── Currency.java                 # INR, USD
│           ├── ExchangeStatus.java           # ACTIVE, INACTIVE
│           ├── IntegrationStatus.java        # DISABLED, ENABLED
│           ├── KycStatus.java                # PENDING, APPROVED, REJECTED
│           ├── OptionType.java               # CE (Call), PE (Put)
│           ├── OrderSide.java                # BUY, SELL
│           ├── OrderStatus.java              # NEW, ACCEPTED, WORKING, PARTIAL, FILLED, etc.
│           ├── OrderType.java                # MARKET, LIMIT, STOP, STOP_LIMIT
│           ├── SettlementKind.java           # EOD, VARIATION, EXPIRY
│           ├── SettlementStatus.java         # CREATED, PROCESSED, REVERSED
│           └── Tif.java                      # DAY, IOC, GTC
│
├── 🗄️ Repository Layer (Spring Data JPA)
│   └── repository/
│       ├── UserRepository.java
│       ├── AuthorityRepository.java
│       ├── ExchangeRepository.java
│       ├── BrokerRepository.java
│       ├── BrokerDeskRepository.java
│       ├── TraderProfileRepository.java
│       ├── TradingAccountRepository.java
│       ├── InstrumentRepository.java
│       ├── ContractRepository.java
│       ├── OrderRepository.java
│       ├── ExecutionRepository.java
│       ├── PositionRepository.java
│       ├── LotRepository.java
│       ├── LedgerEntryRepository.java
│       ├── MarginRuleRepository.java
│       ├── RiskAlertRepository.java
│       ├── SettlementBatchRepository.java
│       └── ... (20 repositories total)
│
├── 🧠 Service Layer (Business Logic)
│   └── service/
│       ├── ExchangeService.java              # Exchange management
│       ├── BrokerService.java                # Broker operations
│       ├── TraderProfileService.java         # Trader account management
│       ├── TradingAccountService.java        # Account & balance operations
│       │
│       ├── InstrumentService.java            # Instrument CRUD
│       ├── ContractService.java              # Derivatives management
│       │
│       ├── OrderService.java                 # Order placement & management
│       ├── ExecutionService.java             # Trade execution
│       ├── PositionService.java              # Position tracking & P&L
│       ├── LotService.java                   # Lot-level accounting
│       │
│       ├── LedgerEntryService.java           # Ledger posting
│       │
│       ├── MarginRuleService.java            # Margin calculation
│       ├── RiskAlertService.java             # Risk monitoring
│       │
│       ├── SettlementBatchService.java       # EOD settlement
│       ├── CorporateActionService.java       # Corporate actions
│       │
│       ├── dto/                              # Data Transfer Objects
│       │   ├── ExchangeDTO.java
│       │   ├── OrderDTO.java
│       │   ├── PositionDTO.java
│       │   └── ... (20 DTOs)
│       │
│       ├── mapper/                           # MapStruct mappers
│       │   ├── ExchangeMapper.java
│       │   ├── OrderMapper.java
│       │   └── ... (20 mappers)
│       │
│       └── criteria/                         # Query filter criteria
│           ├── ExchangeCriteria.java
│           └── ... (filtering support)
│
├── 🌐 REST API Layer
│   └── web/rest/
│       ├── ExchangeResource.java             # /api/exchanges
│       ├── BrokerResource.java               # /api/brokers
│       ├── TraderProfileResource.java        # /api/trader-profiles
│       ├── TradingAccountResource.java       # /api/trading-accounts
│       ├── InstrumentResource.java           # /api/instruments
│       ├── ContractResource.java             # /api/contracts
│       ├── OrderResource.java                # /api/orders
│       ├── ExecutionResource.java            # /api/executions
│       ├── PositionResource.java             # /api/positions
│       ├── LedgerEntryResource.java          # /api/ledger-entries
│       ├── MarginRuleResource.java           # /api/margin-rules
│       ├── RiskAlertResource.java            # /api/risk-alerts
│       ├── SettlementBatchResource.java      # /api/settlement-batches
│       │
│       ├── AccountResource.java              # /api/account (user profile)
│       ├── UserJWTController.java            # /api/authenticate
│       ├── UserResource.java                 # /api/admin/users
│       │
│       ├── errors/                           # Exception handlers
│       │   ├── ExceptionTranslator.java
│       │   └── FieldErrorVM.java
│       │
│       └── vm/                               # View Models
│           ├── LoginVM.java
│           └── ManagedUserVM.java
│
├── 🔌 WebSocket Layer
│   └── web/websocket/
│       ├── ActivityService.java              # WebSocket activity tracking
│       └── dto/
│           └── ActivityDTO.java              # Activity message DTO
│
├── 🔒 Security
│   └── security/
│       ├── SecurityUtils.java                # Security utilities & helpers
│       ├── DomainUserDetailsService.java     # UserDetailsService implementation
│       ├── AuthoritiesConstants.java         # Role constants (ROLE_TRADER, etc.)
│       ├── SpringSecurityAuditorAware.java   # JPA auditing
│       └── jwt/                              # JWT token management
│           ├── JWTFilter.java
│           ├── TokenProvider.java
│           └── JWTConfigurer.java
│
├── ⚙️ Configuration
│   └── config/
│       ├── ApplicationProperties.java        # Custom app properties
│       ├── Constants.java                    # Application constants
│       │
│       ├── SecurityConfiguration.java        # Spring Security config
│       ├── SecurityJwtConfiguration.java     # JWT security config
│       │
│       ├── WebConfigurer.java                # Web MVC configuration
│       ├── StaticResourcesWebConfiguration.java
│       │
│       ├── WebsocketConfiguration.java       # STOMP WebSocket config
│       ├── WebsocketSecurityConfiguration.java
│       │
│       ├── DatabaseConfiguration.java        # JPA & Hibernate config
│       ├── LiquibaseConfiguration.java       # Database migrations
│       │
│       ├── OpenApiConfiguration.java         # Swagger/OpenAPI config
│       ├── JacksonConfiguration.java         # JSON serialization
│       ├── DateTimeFormatConfiguration.java  # Date/time formatting
│       │
│       ├── AsyncConfiguration.java           # Async method execution
│       ├── LoggingConfiguration.java         # Logback configuration
│       └── LoggingAspectConfiguration.java   # AOP logging
│
├── 📊 Monitoring & Management
│   └── management/
│       └── SecurityMetersService.java        # Security metrics
│
└── 🔧 AOP (Aspect-Oriented Programming)
    └── aop/logging/
        └── LoggingAspect.java                # Method execution logging
```

### Frontend React Structure

```
src/main/webapp/app/
│
├── 🚀 Application Root
│   ├── index.tsx                             # React root entry point
│   ├── app.tsx                               # Main App component with routing
│   ├── routes.tsx                            # Global route definitions
│   ├── typings.d.ts                          # TypeScript type declarations
│   ├── setup-tests.ts                        # Jest setup
│   ├── app.scss                              # Global styles
│   └── _bootstrap-variables.scss             # Bootstrap overrides
│
├── 🧩 Entities (CRUD UIs for 20 domain entities)
│   └── entities/
│       ├── menu.tsx                          # Entity menu component
│       ├── reducers.ts                       # Combined entity reducers
│       ├── routes.tsx                        # Entity routing
│       │
│       ├── exchange/
│       │   ├── index.tsx                     # Barrel export
│       │   ├── exchange.tsx                  # List/table view
│       │   ├── exchange-detail.tsx           # Detail/read view
│       │   ├── exchange-update.tsx           # Create/edit form
│       │   ├── exchange-delete-dialog.tsx    # Delete confirmation
│       │   ├── exchange.reducer.ts           # Redux slice
│       │   └── exchange-reducer.spec.ts      # Unit tests
│       │
│       ├── broker/                           # Same pattern
│       ├── broker-desk/
│       ├── exchange-operator/
│       ├── trader-profile/
│       ├── trading-account/
│       │
│       ├── instrument/
│       ├── contract/
│       ├── daily-settlement-price/
│       │
│       ├── order/
│       ├── execution/
│       ├── position/
│       ├── lot/
│       │
│       ├── ledger-entry/
│       │
│       ├── margin-rule/
│       ├── risk-alert/
│       │
│       ├── corporate-action/
│       └── settlement-batch/
│
├── 📄 Feature Modules
│   └── modules/
│       ├── home/
│       │   ├── home.tsx                      # Landing page
│       │   └── home.scss
│       │
│       ├── login/
│       │   ├── login.tsx                     # Login form
│       │   ├── logout.tsx                    # Logout handler
│       │   └── login-modal.tsx               # Modal login
│       │
│       ├── account/                          # User account management
│       │   ├── index.tsx
│       │   ├── activate/
│       │   │   └── activate.tsx              # Email activation
│       │   ├── password/
│       │   │   ├── password.tsx              # Change password
│       │   │   └── password-reset/
│       │   │       ├── init/                 # Request reset
│       │   │       └── finish/               # Complete reset
│       │   ├── register/
│       │   │   └── register.tsx              # User registration
│       │   ├── settings/
│       │   │   └── settings.tsx              # User preferences
│       │   ├── sessions/
│       │   │   └── sessions.tsx              # Active sessions
│       │   └── account.reducer.ts
│       │
│       └── administration/                   # Admin portal
│           ├── index.tsx
│           ├── user-management/              # User CRUD (admin)
│           │   ├── user-management.tsx
│           │   ├── user-management-detail.tsx
│           │   ├── user-management-update.tsx
│           │   ├── user-management-delete-dialog.tsx
│           │   └── user-management.reducer.ts
│           │
│           ├── health/                       # Health checks
│           │   ├── health.tsx
│           │   ├── health-modal.tsx
│           │   └── health.reducer.ts
│           │
│           ├── metrics/                      # JVM metrics
│           │   ├── metrics.tsx
│           │   └── metrics.reducer.ts
│           │
│           ├── logs/                         # Logger management
│           │   ├── logs.tsx
│           │   └── logs.reducer.ts
│           │
│           ├── configuration/                # Spring config viewer
│           │   ├── configuration.tsx
│           │   └── configuration.reducer.ts
│           │
│           └── tracker/                      # User activity tracker
│               ├── tracker.tsx
│               └── tracker.reducer.ts
│
├── 🔧 Shared Components & Utilities
│   └── shared/
│       ├── layout/
│       │   ├── header/
│       │   │   ├── header.tsx                # Top navigation bar
│       │   │   ├── header-components.tsx     # Nav components
│       │   │   └── header.scss
│       │   ├── footer/
│       │   │   └── footer.tsx
│       │   ├── menus/
│       │   │   ├── account.tsx               # Account dropdown
│       │   │   ├── admin.tsx                 # Admin menu
│       │   │   ├── entities.tsx              # Entities menu
│       │   │   ├── locale.tsx                # Language selector
│       │   │   └── menu-components.tsx
│       │   ├── password/
│       │   │   └── password-strength-bar.tsx
│       │   └── sidebar.tsx
│       │
│       ├── model/                            # TypeScript interfaces
│       │   ├── user.model.ts
│       │   ├── exchange.model.ts
│       │   ├── broker.model.ts
│       │   ├── trader-profile.model.ts
│       │   ├── trading-account.model.ts
│       │   ├── instrument.model.ts
│       │   ├── contract.model.ts
│       │   ├── order.model.ts
│       │   ├── execution.model.ts
│       │   ├── position.model.ts
│       │   ├── lot.model.ts
│       │   ├── ledger-entry.model.ts
│       │   ├── margin-rule.model.ts
│       │   ├── risk-alert.model.ts
│       │   ├── settlement-batch.model.ts
│       │   ├── corporate-action.model.ts
│       │   └── ... (38 model files)
│       │
│       ├── reducers/                         # Redux infrastructure
│       │   ├── index.ts                      # Root reducer
│       │   ├── authentication.ts             # Auth state
│       │   ├── application-profile.ts        # App profile
│       │   ├── locale.ts                     # i18n state
│       │   ├── user-management.ts            # User admin
│       │   └── administration.ts             # Admin features
│       │
│       ├── util/                             # Utility functions
│       │   ├── date-utils.ts                 # Date formatting
│       │   ├── entity-utils.ts               # Entity helpers
│       │   ├── pagination.constants.ts       # Pagination config
│       │   └── url-utils.ts                  # URL helpers
│       │
│       ├── auth/
│       │   ├── private-route.tsx             # Protected route wrapper
│       │   └── hasAnyAuthority.tsx           # Role-based rendering
│       │
│       ├── error/
│       │   ├── error-boundary.tsx            # React error boundary
│       │   ├── error-boundary-routes.tsx     # Route error handling
│       │   ├── page-not-found.tsx            # 404 page
│       │   └── error-alert.tsx               # Error notification
│       │
│       └── DurationFormat.tsx                # Duration formatter
│
└── 🔌 Configuration & Middleware
    └── config/
        ├── store.ts                          # Redux store configuration
        │
        ├── constants.ts                      # App constants
        │
        ├── axios-interceptor.ts              # HTTP interceptors (JWT injection)
        ├── axios-interceptor.spec.ts
        │
        ├── error-middleware.ts               # Redux error handling
        ├── notification-middleware.ts        # Toast notifications
        ├── notification-middleware.spec.ts
        │
        ├── logger-middleware.ts              # Redux logger
        │
        ├── websocket-middleware.ts           # WebSocket (STOMP) integration
        │
        ├── translation.ts                    # i18n configuration
        ├── dayjs.ts                          # Date library setup
        │
        └── icon-loader.ts                    # Font Awesome icons
```

### Test Structure

```
src/test/
│
├── 🧪 Backend Tests (Java)
│   └── java/com/rnexchange/
│       ├── IntegrationTest.java              # Base integration test annotation
│       ├── TestUtil.java                     # Test utilities
│       │
│       ├── domain/                           # Entity tests
│       │   ├── ExchangeTest.java
│       │   ├── OrderTest.java
│       │   └── ... (20 entity tests)
│       │
│       ├── repository/                       # Repository tests
│       │   └── timezone/                     # Timezone handling tests
│       │
│       ├── service/                          # Service layer tests
│       │   ├── dto/                          # DTO mapping tests
│       │   └── mapper/                       # MapStruct mapper tests
│       │
│       ├── web/rest/                         # REST API tests (MockMvc)
│       │   ├── ExchangeResourceIT.java
│       │   ├── BrokerResourceIT.java
│       │   ├── OrderResourceIT.java
│       │   ├── AccountResourceIT.java
│       │   ├── UserJWTControllerIT.java
│       │   └── ... (25 REST test classes)
│       │
│       ├── security/                         # Security tests
│       │   ├── DomainUserDetailsServiceIT.java
│       │   └── jwt/
│       │       └── TokenProviderTest.java
│       │
│       └── config/                           # Configuration tests
│           ├── AsyncSyncConfiguration.java
│           ├── SpringBootTestClassOrderer.java
│           └── WebConfigurerTest.java
│
├── 🔄 BDD Tests (Cucumber)
│   └── resources/com/rnexchange/
│       └── cucumber.feature                  # BDD scenarios (to be expanded)
│
├── 🌐 E2E Tests (Cypress)
│   └── javascript/cypress/
│       ├── e2e/
│       │   ├── account/                      # Account management tests
│       │   │   ├── login-page.cy.ts
│       │   │   ├── register-page.cy.ts
│       │   │   ├── settings-page.cy.ts
│       │   │   └── password-page.cy.ts
│       │   │
│       │   ├── administration/               # Admin UI tests
│       │   │   ├── administration.cy.ts
│       │   │   └── user-management.cy.ts
│       │   │
│       │   └── entity/                       # Entity CRUD tests
│       │       ├── exchange.cy.ts
│       │       ├── broker.cy.ts
│       │       ├── order.cy.ts
│       │       └── ... (20 entity test files)
│       │
│       ├── support/
│       │   ├── commands.ts                   # Custom Cypress commands
│       │   ├── entity.ts                     # Entity test helpers
│       │   └── navbar.ts                     # Navigation helpers
│       │
│       └── tsconfig.json
│
└── ⚡ Performance Tests (Gatling)
    └── gatling/
        ├── conf/
        │   ├── gatling.conf                  # Gatling configuration
        │   └── logback.xml
        │
        └── simulations/                      # Load test scenarios
            ├── ExchangeGatlingTest.java
            ├── BrokerGatlingTest.java
            ├── OrderGatlingTest.java
            ├── TradingAccountGatlingTest.java
            └── ... (20 Gatling test classes)
```

---

## 🚀 Enhanced Quickstart Guide

### Option 1: Quick Demo (5 minutes) — H2 In-Memory

**Perfect for**: First-time exploration, no external dependencies

```bash
# 1. Clone repository
git clone <repository-url>
cd rnexchange

# 2. Build and run (Maven downloads everything)
./mvnw spring-boot:run

# 3. In another terminal, start frontend
./npmw start

# 4. Access application
# - Frontend: http://localhost:9000
# - Backend API: http://localhost:8080/api
# - H2 Console: http://localhost:8080/h2-console
# - Swagger UI: http://localhost:8080/swagger-ui/

# 5. Login
# Username: admin
# Password: admin
```

**What you get**:

- ✅ H2 disk database (data persists between restarts)
- ✅ 20 pre-configured entities with CRUD UIs
- ✅ Real-time WebSocket support
- ✅ Full authentication & authorization
- ✅ Sample data (if seed scripts are present)

---

### Option 2: Production-Like Setup (10 minutes) — PostgreSQL

**Perfect for**: Serious development, team environments, realistic testing

#### Step 1: Prerequisites Check

```bash
# Check Java version (need 21+)
java -version

# Check Node.js (need 20.x+)
node --version

# Check Docker (for PostgreSQL)
docker --version
docker compose version

# Check Maven (or use ./mvnw)
mvn -version
```

#### Step 2: Database Setup

```bash
# Start PostgreSQL via Docker Compose
docker compose -f src/main/docker/postgresql.yml up -d

# Verify it's running
docker compose -f src/main/docker/postgresql.yml ps

# View logs if needed
docker compose -f src/main/docker/postgresql.yml logs -f
```

**Database Details**:

- Host: `localhost:5432`
- Database: `rnexchange`
- Username: `rnexchange`
- Password: `rnexchange`

#### Step 3: Build & Install

```bash
# Clean build with all tests
./mvnw clean verify

# Install Node dependencies
./npmw install

# (Optional) Skip tests for faster build
./mvnw clean install -DskipTests
```

#### Step 4: Run Application

**Terminal 1 - Backend** (Spring Boot):

```bash
# Run with 'prod' profile (uses PostgreSQL)
./mvnw -Pprod spring-boot:run

# Or run with 'dev' profile (uses H2, but can override)
./mvnw spring-boot:run
```

**Terminal 2 - Frontend** (Webpack Dev Server):

```bash
# Start React dev server with hot reload
./npmw start
```

**Terminal 3 - Monitoring** (Optional):

```bash
# Start JHipster Control Center
docker compose -f src/main/docker/jhipster-control-center.yml up -d

# Access at: http://localhost:7419
```

#### Step 5: Verify Installation

1. **Check Backend Health**:

   ```bash
   curl http://localhost:8080/management/health
   # Should return: {"status":"UP"}
   ```

2. **Check Frontend**:

   - Open http://localhost:9000
   - Should see RNExchange login page

3. **Check Database**:

   ```bash
   docker exec -it rnexchange-postgresql psql -U rnexchange -d rnexchange
   \dt  # List tables (should see 25+ tables)
   \q   # Quit
   ```

4. **Check WebSocket**:
   - Open browser DevTools → Network → WS
   - Login to app
   - Should see WebSocket connection to `/websocket/tracker`

#### Step 6: Login & Explore

**Default Accounts**:

| Role  | Username | Password | Authorities               |
| ----- | -------- | -------- | ------------------------- |
| Admin | `admin`  | `admin`  | `ROLE_ADMIN`, `ROLE_USER` |
| User  | `user`   | `user`   | `ROLE_USER`               |

**What to Try**:

1. **Administration** → **User Management** (admin only)
2. **Entities** → Browse all 20 entities
3. Create sample data:
   - Exchange → Create "NSE" exchange
   - Broker → Create broker under NSE
   - Instrument → Create "RELIANCE" instrument
4. **API** → Visit http://localhost:8080/swagger-ui/ to test REST APIs

---

### Option 3: Full Production Build (15 minutes)

**Perfect for**: Deployment preparation, CI/CD testing

```bash
# 1. Production build with optimization
./mvnw -Pprod clean verify

# 2. Run production JAR
java -jar target/*.jar

# 3. Access at http://localhost:8080

# 4. (Alternative) Build Docker image
npm run java:docker

# 5. Run with Docker Compose
docker compose -f src/main/docker/app.yml up -d

# 6. View logs
docker compose -f src/main/docker/app.yml logs -f rnexchange-app
```

**Production Optimizations Applied**:

- ✅ Minified JavaScript/CSS bundles
- ✅ Production React build (no warnings)
- ✅ Gzip compression enabled
- ✅ Cache headers configured
- ✅ Security headers enabled
- ✅ Actuator endpoints secured

---

### Option 4: Development with Monitoring (20 minutes)

**Perfect for**: Full observability, performance tuning, debugging

```bash
# 1. Start full monitoring stack
docker compose -f src/main/docker/monitoring.yml up -d

# 2. Start PostgreSQL
docker compose -f src/main/docker/postgresql.yml up -d

# 3. Start SonarQube (code quality)
docker compose -f src/main/docker/sonar.yml up -d

# 4. Run application
./mvnw spring-boot:run     # Terminal 1
./npmw start               # Terminal 2

# 5. Access monitoring tools
```

**Monitoring Dashboard URLs**:

| Tool                 | URL                   | Purpose               |
| -------------------- | --------------------- | --------------------- |
| **Application**      | http://localhost:9000 | Main app              |
| **Prometheus**       | http://localhost:9090 | Metrics collection    |
| **Grafana**          | http://localhost:3000 | Metrics visualization |
| **Alertmanager**     | http://localhost:9093 | Alert management      |
| **SonarQube**        | http://localhost:9001 | Code quality          |
| **JHipster Console** | http://localhost:7419 | App management        |

**Run Code Quality Check**:

```bash
./mvnw -Pprod clean verify sonar:sonar -Dsonar.login=admin -Dsonar.password=admin
```

---

## 📋 Complete Development Workflow

### 1. Daily Development Workflow

```bash
# Morning: Update from main
git checkout main
git pull origin main
git checkout -b feature/my-feature

# Start development environment
docker compose -f src/main/docker/postgresql.yml up -d
./mvnw spring-boot:run  # Terminal 1
./npmw start            # Terminal 2

# Make changes, save files (hot reload works)

# Run tests frequently
./mvnw test             # Backend tests
./npmw test             # Frontend tests

# Before commit: Check code quality
./mvnw checkstyle:check
./npmw run lint

# Commit with conventional commit format
git add .
git commit -m "feat(orders): add market order validation"

# Push and create PR
git push origin feature/my-feature
```

### 2. Feature Development Workflow (Constitution-Compliant)

```bash
# Step 1: Create feature specification
mkdir -p specs/001-market-watch
vim specs/001-market-watch/spec.md
# (Document user stories, acceptance criteria)

# Step 2: Run speckit.plan (if available)
# This generates: research.md, data-model.md, contracts/, tasks.md

# Step 3: Create feature branch
git checkout -b 001-market-watch

# Step 4: TDD Cycle - Write failing tests FIRST
vim src/test/java/com/rnexchange/service/MarketWatchServiceTest.java
./mvnw test -Dtest=MarketWatchServiceTest
# ❌ Tests should FAIL

# Step 5: Implement minimum code to pass
vim src/main/java/com/rnexchange/service/MarketWatchService.java
./mvnw test -Dtest=MarketWatchServiceTest
# ✅ Tests should PASS

# Step 6: Refactor
# Improve code quality, extract methods, add comments

# Step 7: Integration tests
vim src/test/java/com/rnexchange/web/rest/MarketWatchResourceIT.java
./mvnw test -Dtest=MarketWatchResourceIT

# Step 8: Frontend tests
vim src/main/webapp/app/modules/market-watch/market-watch.spec.tsx
./npmw test -- market-watch

# Step 9: E2E test
vim src/test/javascript/cypress/e2e/market-watch.cy.ts
./npmw run e2e

# Step 10: Code quality check
./mvnw verify
./npmw run lint
./npmw run prettier:check

# Step 11: Commit and PR
git add .
git commit -m "feat(market-watch): implement real-time market watch component"
git push origin 001-market-watch
# Create PR on GitHub/GitLab
```

### 3. Entity Modification Workflow

```bash
# Step 1: Edit JDL file
vim rnexchange.jdl
# Example: Add field "description String" to Instrument entity

# Step 2: Regenerate entity
jhipster jdl rnexchange.jdl

# Step 3: Review generated files
git status
git diff

# Files changed:
# - domain/Instrument.java (new field)
# - service/dto/InstrumentDTO.java (new field)
# - resources/config/liquibase/changelog/*.xml (migration)
# - webapp/app/entities/instrument/*.tsx (UI updated)

# Step 4: Update tests
vim src/test/java/com/rnexchange/domain/InstrumentTest.java
vim src/test/java/com/rnexchange/web/rest/InstrumentResourceIT.java

# Step 5: Run tests
./mvnw test
./npmw test

# Step 6: Commit
git add .
git commit -m "feat(instrument): add description field to instrument entity"
```

### 4. Database Migration Workflow

```bash
# Step 1: Make changes to entities (via JDL)
jhipster jdl rnexchange.jdl

# Step 2: Liquibase generates changelog automatically
# Check: src/main/resources/config/liquibase/changelog/

# Step 3: Apply migration (dev)
./mvnw liquibase:update

# Step 4: Verify migration
./mvnw liquibase:status

# Step 5: If wrong, rollback
./mvnw liquibase:rollback -Dliquibase.rollbackCount=1

# Step 6: Test with clean database
./mvnw clean
rm -rf target/h2db/
./mvnw spring-boot:run
# Liquibase will apply all migrations from scratch

# Step 7: Production migration (after deploy)
# Liquibase runs automatically on startup
# Or manually: java -jar app.jar --spring.liquibase.enabled=true
```

### 5. API-First Development Workflow

```bash
# Step 1: Edit OpenAPI specification
vim src/main/resources/swagger/api.yml
# Add new endpoint:
#   /api/market-data/live:
#     get:
#       operationId: getLiveMarketData
#       ...

# Step 2: Generate API code
./mvnw generate-sources

# Step 3: Generated files appear
ls target/generated-sources/openapi/src/main/java/com/rnexchange/web/api/
# - MarketDataApi.java (interface)
# - MarketDataApiDelegate.java (delegate interface)

# Step 4: Implement delegate
vim src/main/java/com/rnexchange/service/MarketDataApiDelegateImpl.java
# @Service
# public class MarketDataApiDelegateImpl implements MarketDataApiDelegate {
#     @Override
#     public ResponseEntity<MarketDataDTO> getLiveMarketData() {
#         ...
#     }
# }

# Step 5: Test via Swagger UI
./mvnw spring-boot:run
# Open: http://localhost:8080/swagger-ui/
# Try endpoint

# Step 6: Write tests
vim src/test/java/com/rnexchange/web/api/MarketDataApiIT.java
```

### 6. WebSocket Development Workflow

```bash
# Step 1: Configure topic in WebsocketConfiguration.java
vim src/main/java/com/rnexchange/config/WebsocketConfiguration.java
# registry.enableSimpleBroker("/topic");
# registry.setApplicationDestinationPrefixes("/app");

# Step 2: Create WebSocket service
vim src/main/java/com/rnexchange/service/MarketDataWebSocketService.java
# @Service
# public class MarketDataWebSocketService {
#     @Autowired private SimpMessagingTemplate messagingTemplate;
#
#     public void sendMarketData(String symbol, MarketDataDTO data) {
#         messagingTemplate.convertAndSend("/topic/market-data." + symbol, data);
#     }
# }

# Step 3: Subscribe in React
vim src/main/webapp/app/modules/market-watch/market-watch.tsx
# import SockJS from 'sockjs-client';
# import { Stomp } from '@stomp/stompjs';
#
# const socket = new SockJS('/websocket/tracker');
# const stompClient = Stomp.over(socket);
# stompClient.connect({}, () => {
#   stompClient.subscribe('/topic/market-data.RELIANCE', (message) => {
#     const data = JSON.parse(message.body);
#     console.log('Received:', data);
#   });
# });

# Step 4: Test
./mvnw spring-boot:run
./npmw start
# Open browser DevTools → Network → WS tab
# Should see WebSocket connection and messages
```

---

## 📝 License

This project is an educational trading simulator. See the Terms of Use in the application for details.

**IMPORTANT DISCLAIMER**: This is a simulated environment. No real money or securities are involved. All trading is for educational and training purposes only.

---

## 🆘 Support & Contact

- **Issues**: Report bugs and feature requests via GitHub Issues
- **Documentation**: See `Project Brief/` for detailed specifications
- **Constitution**: Review [project principles](.specify/memory/constitution.md) for governance

---

**Built with ❤️ using JHipster 8.11.0**  
**Governed by Constitution v1.0.0**  
**Current Milestone: M1 (Market Data Mock Engine)**
