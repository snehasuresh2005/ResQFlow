# ResQFlow Technical Interview Guide

This guide compiles critical architectural questions and answers likely to be discussed in system design or backend engineering interviews regarding **ResQFlow**.

---

### Q1: How do you prevent concurrent coordinators from double-allocating the same resource?
**Answer**:
We use JPA Optimistic Locking combined with Database Transaction boundaries.
1. The `Resource` entity has a `@Version` field.
2. In a single transaction block (`@Transactional`), we query availability, determine the allocation amount, and subtract the quantity from the resource.
3. Upon commit, Hibernate issues an `UPDATE ... SET quantity = :newQty, version = version + 1 WHERE id = :id AND version = :oldVersion`.
4. If another thread committed a quantity update in the meantime, the version check fails and Hibernate throws an `ObjectOptimisticLockingFailureException`.
5. The [GlobalExceptionHandler](file:///c:/Users/Sneha/projects/ResQFlow/backend/src/main/java/com/resqflow/common/exception/GlobalExceptionHandler.java) intercepts this exception and returns a standardized `409 Conflict` response to the client, prompting a retry.

---

### Q2: Explain the time complexity of your routing algorithms.
**Answer**:
- **Dijkstra's Algorithm**: Implemented with a min-priority queue (using travel time as edge weight). Its time complexity is $O((V + E) \log V)$ where $V$ is the number of nodes (depots, shelters, zones) and $E$ is the number of edges (roads).
- **A\* Algorithm**: Uses Dijkstra's base structure but adds a Haversine distance heuristic $h(n)$ to target the destination. Average time complexity is much lower than Dijkstra ($O(V \log V)$ in practice) as it prunes searches away from the opposite direction of the destination node.

---

### Q3: Why did you implement the Transactional Outbox Pattern instead of publishing directly to Kafka inside the service?
**Answer**:
Direct publishing inside a transactional service introduces the **dual-write problem**. If the database transaction commits successfully but the Kafka connection fails, the event is lost. Conversely, if the Kafka message is published first, but the database transaction rolls back, we broadcast a ghost event.
By writing events to the `outbox_events` table *in the same database transaction* as the entity edits, we guarantee atomic persistence. A separate scheduler [OutboxPublisherService](file:///c:/Users/Sneha/projects/ResQFlow/backend/src/main/java/com/resqflow/infrastructure/messaging/OutboxPublisherService.java) polls this table and streams them to Kafka, ensuring **at-least-once delivery**.

---

### Q4: How is dynamic rerouting structured when a road is blocked?
**Answer**:
1. An incident (e.g. tree block) marks a Road status to `BLOCKED`.
2. We write a `RoadBlockedEvent` to the outbox.
3. The [IncidentService](file:///c:/Users/Sneha/projects/ResQFlow/backend/src/main/java/com/resqflow/application/service/IncidentService.java) scans all active missions currently in `IN_TRANSIT` status.
4. It checks the route nodes of each mission. If any adjacent pair of nodes matches the blocked road endpoints, the mission is marked `BLOCKED`.
5. We recalculate the route from the mission's last known checkpoint, skipping the blocked road, transition the mission to `REROUTING`, update the route, and transition it back to `IN_TRANSIT`.

---

### Q5: How do you optimize dashboard performance to handle high request traffic?
**Answer**:
We use **Redis read-through caching** for stats.
Instead of querying count queries across millions of requests, vehicles, and missions every time the dashboard loads, we:
1. Cache metric counts inside Redis keys (e.g. `metrics:total_requests`).
2. Serve stats directly from Redis.
3. Configure our [DashboardUpdateConsumer](file:///c:/Users/Sneha/projects/ResQFlow/backend/src/main/java/com/resqflow/infrastructure/messaging/DashboardUpdateConsumer.java) to listen to Kafka events and increment/decrement Redis values asynchronously.
This offloads read operations from PostgreSQL, keeping dashboard responses sub-millisecond.
