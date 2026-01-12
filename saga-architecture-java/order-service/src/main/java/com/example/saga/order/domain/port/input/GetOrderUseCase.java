package com.example.saga.order.domain.port.input;

import com.example.saga.order.domain.model.Order;
import com.example.saga.order.domain.model.OrderId;

import java.util.Optional;

/**
 * Puerto de entrada para consultar órdenes.
 */
public interface GetOrderUseCase {

    /**
     * Obtiene una orden por su ID.
     *
     * @param orderId ID de la orden a buscar
     * @return La orden si existe
     */
    Optional<Order> execute(OrderId orderId);
}
