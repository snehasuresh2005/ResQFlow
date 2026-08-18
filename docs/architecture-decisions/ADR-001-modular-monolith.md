# ADR-001: Modular Monolith Architecture

## Context
Emergency resource coordination requires consistent state, transactions, and fast local integrations. Deciding between microservices and monolith architectures impacts developer speed, operational complexity, and data consistency.

## Decision
We chose a **Modular Monolith** architecture.
- Domain boundaries (User, Resource, Request, Vehicle, Mission) are separated strictly into package spaces.
- Database access is shared but structured through clean repositories.
- Internal eventing mimics microservice decouplings, easing future microservice refactoring if scale requires it.

## Consequences
- Single deployable container.
- Transactions span multiple domains easily.
- Minimal operational overhead compared to a microservices cluster.
- No network latency between domains.
