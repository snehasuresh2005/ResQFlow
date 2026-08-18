package com.resqflow.domain.event;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    private UUID id;

    @NotBlank
    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @NotBlank
    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @NotBlank
    @Column(name = "event_type", nullable = false)
    private String eventType;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean processed = false;
}
