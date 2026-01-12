package com.example.saga.order.infrastructure.adapter.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositorio JPA Spring Data.
 * Implementación automática de operaciones CRUD.
 */
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {
}
