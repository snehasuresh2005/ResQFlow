# ResQFlow Design Patterns

This document details the architectural design patterns implemented in **ResQFlow** to solve emergency logistics coordination problems.

---

## 1. Strategy Pattern
### Problem Solved
coordinators need to allocate resources using different policies (e.g. proximity-aware, expiry-aware, or balanced distribution) depending on the context. Hardcoding these rules results in massive if-else branches.

### Solution
We define the [AllocationStrategy](file:///c:/Users/Sneha/projects/ResQFlow/backend/src/main/java/com/resqflow/application/strategy/AllocationStrategy.java) interface, allowing runtime interchangeable algorithms:
- `NearestResourceStrategy` (greedily chooses closest depots)
- `ExpiryAwareStrategy` (targets soonest to expire stocks)
- `HybridAllocationStrategy` (evaluates composite weighted multi-criteria scores)

### Classes Involved
- `AllocationStrategy` (Interface)
- `NearestResourceStrategy`, `ExpiryAwareStrategy`, `HybridAllocationStrategy`, `HighestPriorityStrategy`, `FairDistributionStrategy` (Implementations)
- `AllocationStrategyFactory` (Registry/Factory)

### Code Example
```java
public interface AllocationStrategy {
    AllocationResult allocate(
            EmergencyRequest request,
            String resourceType,
            Double quantityNeeded,
            List<Resource> resources,
            AllocationContext context
    );
}
```

---

## 2. Factory Pattern
### Problem Solved
Resource types (Food, Water, Medical) and Vehicle types (Truck, Van, Ambulance, Boat) are modeled via inheritance. Clients shouldn't manually instantiate concrete subclasses.

### Solution
We employ static factory methods in [ResourceFactory](file:///c:/Users/Sneha/projects/ResQFlow/backend/src/main/java/com/resqflow/application/factory/ResourceFactory.java) and [VehicleFactory](file:///c:/Users/Sneha/projects/ResQFlow/backend/src/main/java/com/resqflow/application/factory/VehicleFactory.java) to instanciate the correct sub-class based on metadata.

### Classes Involved
- `ResourceFactory`
- `VehicleFactory`
- `MissionStateFactory`

### Code Example
```java
public class ResourceFactory {
    public static Resource createResource(CreateResourceDto dto, Depot depot) {
        return switch (dto.getResourceType().toUpperCase()) {
            case "FOOD" -> new FoodResource();
            case "MEDICAL" -> new MedicalResource();
            default -> new WaterResource();
        };
    }
}
```

---

## 3. State Pattern
### Problem Solved
Logistics missions transition through strict lifecycle steps (CREATED, DISPATCHED, IN_TRANSIT, BLOCKED, REROUTING, DELIVERED, etc.). Allowing arbitrary transitions leads to inconsistent state (e.g., delivered -> in transit).

### Solution
We implement the State pattern via [MissionState](file:///c:/Users/Sneha/projects/ResQFlow/backend/src/main/java/com/resqflow/application/state/MissionState.java) where each state subclass inherits from [BaseMissionState](file:///c:/Users/Sneha/projects/ResQFlow/backend/src/main/java/com/resqflow/application/state/BaseMissionState.java) (throwing exceptions by default) and overrides only valid transitions.

### Classes Involved
- `MissionState` (Interface)
- `BaseMissionState` (Abstract default class)
- `CreatedState`, `DispatchedState`, `InTransitState`, `BlockedState`, `ReroutingState` (Concrete states)

### Code Example
```java
public class BlockedState extends BaseMissionState {
    @Override
    public void reroute(Mission mission, Route newRoute) {
        mission.setRoute(newRoute);
        mission.setStatus("REROUTING");
    }
}
```

---

## 4. Observer Pattern (Event-Driven)
### Problem Solved
Actions like allocating resources or blocking roads require multiple side-effects (auditing, notification broadcasts, dashboard updating) without creating tight coupling.

### Solution
We publish events to Apache Kafka via a Transactional Outbox publisher. Consumers (e.g. [AuditLogConsumer](file:///c:/Users/Sneha/projects/ResQFlow/backend/src/main/java/com/resqflow/infrastructure/messaging/AuditLogConsumer.java), [DashboardUpdateConsumer](file:///c:/Users/Sneha/projects/ResQFlow/backend/src/main/java/com/resqflow/infrastructure/messaging/DashboardUpdateConsumer.java)) act as decoupled observers subscribing to these events.

---

## 5. Repository Pattern
### Problem Solved
Domain logic should remain independent of raw database connection schemas.

### Solution
Spring Data JPA Repository interfaces (e.g., [ResourceRepository](file:///c:/Users/Sneha/projects/ResQFlow/backend/src/main/java/com/resqflow/infrastructure/persistence/ResourceRepository.java)) abstract data retrieval and persistence.
