# ADR-003: Redis for Caching and Idempotency

## Context
Coordinators submit requests that must be processed idempotently. Additionally, the dashboard polls metrics frequently, which can load the database.

## Decision
We chose **Redis 7** to handle:
- **Idempotency storage**: Caches request tokens to ensure identical requests do not trigger multiple allocations.
- **Metrics caching**: Caches total, critical, and active mission counts for sub-millisecond dashboard reads.

## Consequences
- High-speed lookups.
- Reduced CPU load on PostgreSQL.
- Requires managing key expiries.
