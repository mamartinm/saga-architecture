package com.example.saga.order.domain.port.input;

import com.example.saga.order.domain.model.Money;
import com.example.saga.order.domain.model.Order;
import com.example.saga.order.domain.model.ProductId;
import com.example.saga.order.domain.model.UserId;

/**
 * Puerto de entrada para el caso de uso de crear una orden.
 * Define el contrato para la creación de órdenes desde cualquier adaptador.
 */
public interface CreateOrderUseCase {

    /**
     * Crea una nueva orden y dispara la saga.
     *
     * @param command Comando con los datos necesarios para crear la orden
     * @return La orden creada
     */
    Order execute(CreateOrderCommand command);

    /**
     * Comando que encapsula los datos necesarios para crear una orden.
     */
    record CreateOrderCommand(
            UserId userId,
            ProductId productId,
            Money amount) {
        public static CreateOrderCommand of(Integer userId, Integer productId, Double amount) {
            return new CreateOrderCommand(
                    UserId.of(userId),
                    ProductId.of(productId),
                    Money.of(amount));
        }
    }
}
