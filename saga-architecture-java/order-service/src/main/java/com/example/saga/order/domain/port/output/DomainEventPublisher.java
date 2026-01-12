package com.example.saga.order.domain.port.output;

import com.example.saga.order.domain.event.DomainEvent;

import java.util.List;

/**
 * Puerto de salida para publicación de eventos de dominio.
 * Define el contrato para publicar eventos a cualquier sistema de mensajería.
 */
public interface DomainEventPublisher {

    /**
     * Publica un evento de dominio.
     *
     * @param event El evento a publicar
     */
    void publish(DomainEvent event);

    /**
     * Publica múltiples eventos de dominio.
     *
     * @param events Lista de eventos a publicar
     */
    default void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
