package com.example.saga.order.domain.model;

import java.util.Objects;

/**
 * Value Object que representa el identificador de un usuario.
 * Encapsula la validación del ID de usuario.
 */
public record UserId(Integer value) {

    public UserId {
        Objects.requireNonNull(value, "UserId value cannot be null");
        if (value <= 0) {
            throw new IllegalArgumentException("UserId must be a positive integer");
        }
    }

    public static UserId of(Integer value) {
        return new UserId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
