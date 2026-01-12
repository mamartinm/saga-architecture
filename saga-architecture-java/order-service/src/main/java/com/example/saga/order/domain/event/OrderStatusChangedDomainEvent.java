package com.example.saga.order.domain.event;

import com.example.saga.order.domain.model.OrderId;
import com.example.saga.order.domain.model.OrderStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio que indica que el estado de una orden cambió.
 */
public record OrderStatusChangedDomainEvent(
        UUID eventId,
        Instant occurredOn,
        OrderId orderId,
        OrderStatus previousStatus,
        OrderStatus newStatus) implements DomainEvent {

    public OrderStatusChangedDomainEvent(OrderId orderId, OrderStatus previousStatus, OrderStatus newStatus) {
        this(UUID.randomUUID(), Instant.now(), orderId, previousStatus, newStatus);
    }

    @Override
    public String eventType() {
        return "order.status.changed";
    }
}
