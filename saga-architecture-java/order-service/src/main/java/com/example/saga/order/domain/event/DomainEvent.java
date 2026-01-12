package com.example.saga.order.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Interfaz base para todos los eventos de dominio.
 * Define el contrato mínimo que debe cumplir un evento.
 */
public interface DomainEvent {

    /**
     * Identificador único del evento.
     */
    UUID eventId();

    /**
     * Momento en que ocurrió el evento.
     */
    Instant occurredOn();

    /**
     * Tipo de evento para serialización/deserialización.
     */
    String eventType();
}
