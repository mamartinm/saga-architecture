package com.example.saga.order.domain.exception;

import com.example.saga.order.domain.model.OrderStatus;

/**
 * Excepción lanzada cuando se intenta una transición de estado inválida.
 */
public class InvalidOrderStateException extends DomainException {

    private final OrderStatus currentStatus;
    private final String attemptedAction;

    public InvalidOrderStateException(OrderStatus currentStatus, String attemptedAction) {
        super(String.format("Cannot %s order in status %s", attemptedAction, currentStatus));
        this.currentStatus = currentStatus;
        this.attemptedAction = attemptedAction;
    }

    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }

    public String getAttemptedAction() {
        return attemptedAction;
    }
}
