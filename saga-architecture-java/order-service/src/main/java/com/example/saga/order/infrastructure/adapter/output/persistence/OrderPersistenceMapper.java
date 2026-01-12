package com.example.saga.order.infrastructure.adapter.output.persistence;

import com.example.saga.order.domain.model.*;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre entidades de dominio y entidades JPA.
 * Mantiene la separación entre el modelo de dominio y el modelo de
 * persistencia.
 */
@Component
public class OrderPersistenceMapper {

    /**
     * Convierte una entidad de dominio a una entidad JPA.
     */
    public OrderJpaEntity toJpaEntity(Order order) {
        return new OrderJpaEntity(
                order.getId().value(),
                order.getUserId().value(),
                order.getProductId().value(),
                order.getPrice().toDouble(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }

    /**
     * Convierte una entidad JPA a una entidad de dominio.
     * Usa el método reconstitute para evitar generar eventos.
     */
    public Order toDomainEntity(OrderJpaEntity entity) {
        return Order.reconstitute(
                OrderId.of(entity.getId()),
                UserId.of(entity.getUserId()),
                ProductId.of(entity.getProductId()),
                Money.of(entity.getPrice()),
                entity.getOrderStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
