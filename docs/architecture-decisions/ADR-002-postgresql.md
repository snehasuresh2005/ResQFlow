# ADR-002: PostgreSQL as Primary Datastore

## Context
Disaster logistics requires relational safety, transactional consistency (ACID), and robust geographic coordinates capabilities.

## Decision
We chose **PostgreSQL 16**.
- It provides full ACID transaction guarantees needed for stock allocations.
- It supports foreign keys and structured schemas.
- It is highly optimized for indexing geo-coordinates.

## Consequences
- High reliability.
- Relational mapping using Hibernate.
- Support for transactional outbox pattern directly inside database transactions.
