package com.example.saga.order.application.saga;

import com.example.saga.order.domain.exception.OrderNotFoundException;
import com.example.saga.order.domain.model.Order;
import com.example.saga.order.domain.model.OrderId;
import com.example.saga.order.domain.port.output.DomainEventPublisher;
import com.example.saga.order.domain.port.output.InventoryCommandSender;
import com.example.saga.order.domain.port.output.InventoryCommandSender.ReserveInventoryCommand;
import com.example.saga.order.domain.port.output.OrderRepository;
import com.example.saga.order.domain.port.output.PaymentCommandSender;
import com.example.saga.order.domain.port.output.PaymentCommandSender.ProcessPaymentCommand;
import com.example.saga.order.domain.port.output.PaymentCommandSender.RefundPaymentCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestador de la Saga de creación de órdenes.
 * 
 * Este componente:
 * - Coordina el flujo de la saga a través de los diferentes servicios
 * - Maneja las compensaciones en caso de fallo
 * - Actualiza el estado de la orden según los eventos recibidos
 * 
 * Flujo:
 * 1. Orden creada -> Enviar comando de pago
 * 2. Pago completado -> Enviar comando de reserva de inventario
 * 3. Inventario reservado -> Completar orden
 * 
 * Compensaciones:
 * - Pago fallido -> Cancelar orden
 * - Inventario fallido -> Reembolsar pago + Cancelar orden
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderSagaOrchestrator {

    private final OrderRepository orderRepository;
    private final PaymentCommandSender paymentCommandSender;
    private final InventoryCommandSender inventoryCommandSender;
    private final DomainEventPublisher eventPublisher;

    /**
     * Inicia la saga cuando se crea una orden.
     * Primer paso: solicitar el pago.
     */
    @Transactional
    public void onOrderCreated(OrderId orderId) {
        log.info("Saga: Order {} created, initiating payment", orderId);

        Order order = findOrder(orderId);

        // Marcar pago como pendiente
        order.markPaymentPending();
        order = orderRepository.save(order);
        publishEvents(order);

        // Enviar comando de pago
        ProcessPaymentCommand paymentCommand = new ProcessPaymentCommand(
                order.getId(),
                order.getUserId(),
                order.getProductId(),
                order.getPrice());
        paymentCommandSender.sendProcessPaymentCommand(paymentCommand);

        log.info("Saga: Payment command sent for order {}", orderId);
    }

    /**
     * Maneja el evento de pago completado.
     * Siguiente paso: reservar inventario.
     */
    @Transactional
    public void onPaymentCompleted(OrderId orderId) {
        log.info("Saga: Payment completed for order {}", orderId);

        Order order = findOrder(orderId);

        // Confirmar pago y marcar inventario como pendiente
        order.confirmPayment();
        order.markInventoryPending();
        order = orderRepository.save(order);
        publishEvents(order);

        // Enviar comando de reserva de inventario
        ReserveInventoryCommand inventoryCommand = new ReserveInventoryCommand(
                order.getId(),
                order.getUserId(),
                order.getProductId());
        inventoryCommandSender.sendReserveInventoryCommand(inventoryCommand);

        log.info("Saga: Inventory reservation command sent for order {}", orderId);
    }

    /**
     * Maneja el evento de pago fallido.
     * Compensación: cancelar la orden.
     */
    @Transactional
    public void onPaymentFailed(OrderId orderId) {
        log.warn("Saga: Payment failed for order {}", orderId);

        Order order = findOrder(orderId);

        // El dominio maneja la transición: failPayment() auto-cancela
        order.failPayment();
        order = orderRepository.save(order);
        publishEvents(order);

        log.info("Saga: Order {} cancelled due to payment failure", orderId);
    }

    /**
     * Maneja el evento de inventario reservado.
     * Paso final: completar la orden.
     */
    @Transactional
    public void onInventoryReserved(OrderId orderId) {
        log.info("Saga: Inventory reserved for order {}", orderId);

        Order order = findOrder(orderId);

        // Confirmar inventario y completar orden
        order.confirmInventory();
        order.complete();
        order = orderRepository.save(order);
        publishEvents(order);

        log.info("Saga: Order {} completed successfully!", orderId);
    }

    /**
     * Maneja el evento de inventario rechazado.
     * Compensación: reembolsar pago y cancelar orden.
     */
    @Transactional
    public void onInventoryRejected(OrderId orderId) {
        log.warn("Saga: Inventory rejected for order {}", orderId);

        Order order = findOrder(orderId);

        // Marcar fallo de inventario
        order.failInventory();

        // Solicitar reembolso
        RefundPaymentCommand refundCommand = new RefundPaymentCommand(
                order.getId(),
                order.getUserId());
        paymentCommandSender.sendRefundPaymentCommand(refundCommand);

        // Cancelar orden
        order.cancel();
        order = orderRepository.save(order);
        publishEvents(order);

        log.info("Saga: Refund requested and order {} cancelled due to inventory failure", orderId);
    }

    // ==================== PRIVATE HELPERS ====================

    private Order findOrder(OrderId orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private void publishEvents(Order order) {
        eventPublisher.publishAll(order.getDomainEvents());
        order.clearDomainEvents();
    }
}
