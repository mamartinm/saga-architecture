package com.example.saga.order.application.service;

import com.example.saga.order.domain.model.Order;
import com.example.saga.order.domain.port.input.CreateOrderUseCase;
import com.example.saga.order.domain.port.output.DomainEventPublisher;
import com.example.saga.order.domain.port.output.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación para crear órdenes.
 * Implementa el caso de uso CreateOrderUseCase.
 * 
 * Responsabilidades:
 * - Orquesta el flujo de creación de orden
 * - Persiste la orden
 * - Publica eventos de dominio
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CreateOrderApplicationService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public Order execute(CreateOrderCommand command) {
        log.info("Creating order for user: {}, product: {}, amount: {}",
                command.userId(), command.productId(), command.amount());

        // 1. Crear la orden usando el factory method del dominio
        Order order = Order.create(
                command.userId(),
                command.productId(),
                command.amount());

        // 2. Persistir la orden
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getId());

        // 3. Publicar los eventos de dominio generados
        eventPublisher.publishAll(savedOrder.getDomainEvents());
        savedOrder.clearDomainEvents();

        return savedOrder;
    }
}
