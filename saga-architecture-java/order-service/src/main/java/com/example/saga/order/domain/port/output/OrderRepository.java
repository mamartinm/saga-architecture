package com.example.saga.order.domain.port.output;

import com.example.saga.order.domain.model.Order;
import com.example.saga.order.domain.model.OrderId;

import java.util.Optional;

/**
 * Puerto de salida para persistencia de órdenes.
 * Esta interfaz define el contrato que debe implementar cualquier
 * adaptador de persistencia (JPA, MongoDB, etc.)
 * 
 * El dominio depende de esta interfaz, NO de la implementación concreta.
 */
public interface OrderRepository {

    /**
     * Guarda una orden (crear o actualizar).
     *
     * @param order La orden a guardar
     * @return La orden persistida
     */
    Order save(Order order);

    /**
     * Busca una orden por su ID.
     *
     * @param orderId ID de la orden
     * @return La orden si existe
     */
    Optional<Order> findById(OrderId orderId);

    /**
     * Verifica si existe una orden con el ID dado.
     *
     * @param orderId ID de la orden
     * @return true si existe
     */
    boolean existsById(OrderId orderId);
}
