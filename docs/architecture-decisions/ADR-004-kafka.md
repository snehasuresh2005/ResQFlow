# ADR-004: Apache Kafka for Event Streaming

## Context
Multiple auditing, notification, and analytical metrics consumers must listen to system changes without coupling to the main logistics services.

## Decision
We chose **Apache Kafka (Kraft mode)** for event-driven message distribution.
- Kraft mode removes the complexity of managing Zookeeper nodes.
- Partition keying ensures message ordering per entity ID.
- Offloads heavy logging and audit writes from the web thread.

## Consequences
- Guaranteed at-least-once message delivery.
- Asynchronous data processing.
