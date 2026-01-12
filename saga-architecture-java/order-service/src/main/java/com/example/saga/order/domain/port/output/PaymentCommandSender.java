package com.example.saga.order.domain.port.output;

import com.example.saga.order.domain.model.Money;
import com.example.saga.order.domain.model.OrderId;
import com.example.saga.order.domain.model.ProductId;
import com.example.saga.order.domain.model.UserId;

/**
 * Puerto de salida para enviar comandos de pago.
 * Define el contrato para comunicarse con el servicio de pagos.
 */
public interface PaymentCommandSender {

    /**
     * Envía un comando para procesar un pago.
     *
     * @param command Datos del comando de pago
     */
    void sendProcessPaymentCommand(ProcessPaymentCommand command);

    /**
     * Envía un comando para reembolsar un pago.
     *
     * @param command Datos del comando de reembolso
     */
    void sendRefundPaymentCommand(RefundPaymentCommand command);

    /**
     * Comando para procesar un pago.
     */
    record ProcessPaymentCommand(
            OrderId orderId,
            UserId userId,
            ProductId productId,
            Money amount) {
    }

    /**
     * Comando para reembolsar un pago.
     */
    record RefundPaymentCommand(
            OrderId orderId,
            UserId userId) {
    }
}
