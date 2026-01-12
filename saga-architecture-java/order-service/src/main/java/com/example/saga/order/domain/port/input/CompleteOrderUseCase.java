package com.example.saga.order.domain.port.input;

import com.example.saga.order.domain.model.Order;
import com.example.saga.order.domain.model.OrderId;

/**
 * Puerto de entrada para completar una orden.
 */
public interface CompleteOrderUseCase {

    /**
     * Marca una orden como completada.
     *
     * @param orderId ID de la orden a completar
     * @return La orden actualizada
     */
    Order execute(OrderId orderId);
}
