package com.example.saga.order.domain.event;

import com.example.saga.order.domain.model.Money;
import com.example.saga.order.domain.model.OrderId;
import com.example.saga.order.domain.model.ProductId;
import com.example.saga.order.domain.model.UserId;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio que indica que una nueva orden fue creada.
 * Este evento es inmutable y captura el momento exacto de creación.
 */
public record OrderCreatedDomainEvent(
        UUID eventId,
        Instant occurredOn,
        OrderId orderId,
        UserId userId,
        ProductId productId,
        Money amount) implements DomainEvent {

    public OrderCreatedDomainEvent(OrderId orderId, UserId userId, ProductId productId, Money amount) {
        this(UUID.randomUUID(), Instant.now(), orderId, userId, productId, amount);
    }

    @Override
    public String eventType() {
        return "order.created";
    }
}
