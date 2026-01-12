package com.example.saga.order.infrastructure.adapter.output.persistence;

import com.example.saga.order.domain.model.Order;
import com.example.saga.order.domain.model.OrderId;
import com.example.saga.order.domain.port.output.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adaptador de persistencia que implementa el puerto OrderRepository.
 * Actúa como puente entre el dominio y la infraestructura JPA.
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderPersistenceMapper mapper;

    @Override
    public Order save(Order order) {
        log.debug("Saving order: {}", order.getId());

        OrderJpaEntity entity = mapper.toJpaEntity(order);
        OrderJpaEntity savedEntity = jpaRepository.save(entity);

        return mapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        log.debug("Finding order by ID: {}", orderId);

        return jpaRepository.findById(orderId.value())
                .map(mapper::toDomainEntity);
    }

    @Override
    public boolean existsById(OrderId orderId) {
        return jpaRepository.existsById(orderId.value());
    }
}
