package com.example.saga.order.domain.model;

/**
 * Enum que representa los posibles estados de una orden en el dominio.
 * Define las transiciones válidas del ciclo de vida de una orden.
 */
public enum OrderStatus {

    CREATED("Orden creada, pendiente de procesamiento"),
    PAYMENT_PENDING("Esperando confirmación de pago"),
    PAYMENT_COMPLETED("Pago completado exitosamente"),
    PAYMENT_FAILED("Pago fallido"),
    INVENTORY_PENDING("Esperando reserva de inventario"),
    INVENTORY_RESERVED("Inventario reservado exitosamente"),
    INVENTORY_FAILED("Reserva de inventario fallida"),
    COMPLETED("Orden completada exitosamente"),
    CANCELLED("Orden cancelada");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Verifica si la orden puede ser completada desde el estado actual.
     */
    public boolean canComplete() {
        return this == INVENTORY_RESERVED;
    }

    /**
     * Verifica si la orden puede ser cancelada desde el estado actual.
     */
    public boolean canCancel() {
        return this != COMPLETED && this != CANCELLED;
    }

    /**
     * Verifica si la orden está en un estado terminal (final).
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }
}
