package com.example.saga.order.domain.exception;

import com.example.saga.order.domain.model.OrderId;

/**
 * Excepción lanzada cuando no se encuentra una orden.
 */
public class OrderNotFoundException extends DomainException {

    private final OrderId orderId;

    public OrderNotFoundException(OrderId orderId) {
        super(String.format("Order with id %s not found", orderId));
        this.orderId = orderId;
    }

    public OrderId getOrderId() {
        return orderId;
    }
}
