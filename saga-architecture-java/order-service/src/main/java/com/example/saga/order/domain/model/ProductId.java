package com.example.saga.order.domain.model;

import java.util.Objects;

/**
 * Value Object que representa el identificador de un producto.
 * Encapsula la validación del ID de producto.
 */
public record ProductId(Integer value) {

    public ProductId {
        Objects.requireNonNull(value, "ProductId value cannot be null");
        if (value <= 0) {
            throw new IllegalArgumentException("ProductId must be a positive integer");
        }
    }

    public static ProductId of(Integer value) {
        return new ProductId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
