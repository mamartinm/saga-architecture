package com.example.saga.order.infrastructure.adapter.output.messaging;

import com.example.saga.order.domain.event.DomainEvent;
import com.example.saga.order.domain.event.OrderCreatedDomainEvent;
import com.example.saga.order.domain.port.output.DomainEventPublisher;
import com.example.saga.common.OrderEvent;
import com.example.saga.common.OrderRequestDTO;
import com.example.saga.common.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/**
 * Adaptador que implementa la publicación de eventos de dominio a Kafka.
 * Convierte eventos de dominio a eventos de integración para consumidores
 * externos usando MapStruct.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private static final String ORDER_EVENTS_OUT = "order-events-out-0";

    private final StreamBridge streamBridge;
    private final OrderIntegrationMapper mapper;

    @Override
    public void publish(DomainEvent event) {
        log.debug("Publishing domain event: {}", event.eventType());

        if (event instanceof OrderCreatedDomainEvent orderCreated) {
            publishOrderCreatedEvent(orderCreated);
        } else {
            log.debug("Event type {} not handled for external publishing", event.eventType());
        }
    }

    private void publishOrderCreatedEvent(OrderCreatedDomainEvent domainEvent) {
        // Convertir evento de dominio a evento de integración usando mapper
        OrderRequestDTO orderRequest = mapper.toOrderRequest(domainEvent);

        OrderEvent integrationEvent = new OrderEvent(orderRequest, OrderStatus.ORDER_CREATED);

        log.info("Publishing OrderCreated integration event for orderId: {}", domainEvent.orderId());
        streamBridge.send(ORDER_EVENTS_OUT, integrationEvent);
    }
}
