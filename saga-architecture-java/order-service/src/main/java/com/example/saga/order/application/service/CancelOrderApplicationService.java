package com.example.saga.order.application.service;

import com.example.saga.order.domain.exception.OrderNotFoundException;
import com.example.saga.order.domain.model.Order;
import com.example.saga.order.domain.model.OrderId;
import com.example.saga.order.domain.port.input.CancelOrderUseCase;
import com.example.saga.order.domain.port.output.DomainEventPublisher;
import com.example.saga.order.domain.port.output.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación para cancelar órdenes.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CancelOrderApplicationService implements CancelOrderUseCase {

    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public Order execute(OrderId orderId) {
        log.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Ejecutar la lógica de dominio
        order.cancel();

        // Persistir cambios
        Order savedOrder = orderRepository.save(order);

        // Publicar eventos
        eventPublisher.publishAll(savedOrder.getDomainEvents());
        savedOrder.clearDomainEvents();

        log.info("Order {} cancelled", orderId);
        return savedOrder;
    }
}
