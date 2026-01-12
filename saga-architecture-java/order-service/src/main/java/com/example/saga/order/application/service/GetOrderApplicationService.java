package com.example.saga.order.application.service;

import com.example.saga.order.domain.model.Order;
import com.example.saga.order.domain.model.OrderId;
import com.example.saga.order.domain.port.input.GetOrderUseCase;
import com.example.saga.order.domain.port.output.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio de aplicación para consultar órdenes.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GetOrderApplicationService implements GetOrderUseCase {

    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> execute(OrderId orderId) {
        log.debug("Getting order: {}", orderId);
        return orderRepository.findById(orderId);
    }
}
