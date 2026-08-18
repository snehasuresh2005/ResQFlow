# ADR-006: Strategy Pattern for Allocations and Routing

## Context
Allocation logic and path calculations vary depending on the incident type (e.g. floods require water-compatible vehicles, earthquakes require route bypasses). Hardcoding logic prevents extensibility.

## Decision
We chose the **Strategy Design Pattern**.
- Concrete strategies conform to interfaces (`AllocationStrategy` and `RoutingStrategy`).
- Strategies are registered dynamically in factories (`AllocationStrategyFactory` and `RoutingStrategyFactory`).

## Consequences
- Open-Closed Principle (SOLID) compliance.
- Easy integration of new strategies (e.g., DroneAllocationStrategy) without modifying existing codebase core.
