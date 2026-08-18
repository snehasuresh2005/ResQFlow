# ADR-005: Optimistic Locking for Resource Stock Safety

## Context
High-traffic coordination involves multiple coordinators attempting to allocate resources from the same pool. Over-allocation (creating negative quantities) must be prevented.

## Decision
We chose **Optimistic Locking (`@Version` versioning)** over Pessimistic locking.
- Pessimistic locks lock rows which degrades database throughput and can cause deadlocks.
- Optimistic locks assume low contention and throw a fast exception on commit conflicts, which is ideal for stateless API retries.

## Consequences
- No database row lock delays.
- Standard 409 Conflict status response on collision.
