package com.example.saga.order.domain.port.output;

import com.example.saga.order.domain.model.OrderId;
import com.example.saga.order.domain.model.ProductId;
import com.example.saga.order.domain.model.UserId;

/**
 * Puerto de salida para enviar comandos de inventario.
 * Define el contrato para comunicarse con el servicio de inventario.
 */
public interface InventoryCommandSender {

    /**
     * Envía un comando para reservar inventario.
     *
     * @param command Datos del comando de reserva
     */
    void sendReserveInventoryCommand(ReserveInventoryCommand command);

    /**
     * Envía un comando para liberar inventario (compensación).
     *
     * @param command Datos del comando de liberación
     */
    void sendReleaseInventoryCommand(ReleaseInventoryCommand command);

    /**
     * Comando para reservar inventario.
     */
    record ReserveInventoryCommand(
            OrderId orderId,
            UserId userId,
            ProductId productId) {
    }

    /**
     * Comando para liberar inventario.
     */
    record ReleaseInventoryCommand(
            OrderId orderId,
            ProductId productId) {
    }
}
