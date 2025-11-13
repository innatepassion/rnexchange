# Research: Seed Domain Trading Data Baseline

Decision: Adopt Spring Boot integration tests that re-run Liquibase against an embedded PostgreSQL/H2 container to verify seed idempotency (drop → migrate → migrate again) and assert record counts for each seeded aggregate.  
Rationale: Integration tests exercise the full Liquibase pipeline, matching production startup behaviour and catching checksum or duplication regressions early; leveraging the existing `@EmbeddedSQL` test infrastructure keeps the tests consistent with current project patterns.  
Alternatives considered: Adding lightweight unit tests around custom seed services (insufficient because seeding occurs via Liquibase, not service code); manual QA verification (slow, non-repeatable); Liquibase `RollbackTest` command (adds CI overhead without covering entity relationships).

Decision: Replace the `faker` Liquibase context with a dedicated `baseline` context, remove `fake-data` CSV includes from dev profile, and introduce a pre-seed cleanup task (truncate non-system tables) executed via a Spring Boot `CommandLineRunner` guarded by profile.  
Rationale: Using a unique context keeps demo seed documents opt-in while allowing the new changelog to own authoritative baseline data; truncation via managed code ensures consistent behaviour across H2 and PostgreSQL without relying on ad-hoc SQL scripts.  
Alternatives considered: Dropping and recreating the schema via `spring.jpa.hibernate.ddl-auto=create` (violates Liquibase-only migration rule); leaving faker context enabled (risk of mixed demo/domain data); writing raw database-specific truncate SQL in Liquibase (less portable and harder to maintain).
