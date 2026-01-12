package com.example.saga.order.domain.port.input;

import com.example.saga.order.domain.model.Order;
import com.example.saga.order.domain.model.OrderId;

/**
 * Puerto de entrada para cancelar una orden.
 */
public interface CancelOrderUseCase {

    /**
     * Cancela una orden.
     *
     * @param orderId ID de la orden a cancelar
     * @return La orden actualizada
     */
    Order execute(OrderId orderId);
}
