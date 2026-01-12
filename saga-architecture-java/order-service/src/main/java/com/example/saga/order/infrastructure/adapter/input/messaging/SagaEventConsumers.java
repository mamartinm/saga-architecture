package com.example.saga.order.infrastructure.adapter.input.messaging;

import com.example.saga.common.InventoryEvent;
import com.example.saga.common.InventoryStatus;
import com.example.saga.common.OrderEvent;
import com.example.saga.common.OrderStatus;
import com.example.saga.common.PaymentEvent;
import com.example.saga.common.PaymentStatus;
import com.example.saga.order.application.saga.OrderSagaOrchestrator;
import com.example.saga.order.domain.model.OrderId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * Adaptador de entrada para eventos Kafka.
 * Consume eventos de otros microservicios y los traduce a llamadas al
 * orquestador de saga.
 * 
 * Los bindings de Spring Cloud Stream se configuran en application.yml:
 * - orderCreatedConsumer-in-0 -> order-events
 * - paymentEventConsumer-in-0 -> payment-events
 * - inventoryEventConsumer-in-0 -> inventory-events
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class SagaEventConsumers {

    private final OrderSagaOrchestrator sagaOrchestrator;

    /**
     * Consume eventos de orden creada para iniciar la saga.
     */
    @Bean
    public Consumer<OrderEvent> orderCreatedConsumer() {
        return orderEvent -> {
            log.info("Kafka: Received OrderEvent with status: {}", orderEvent.status());

            if (orderEvent.status().equals(OrderStatus.ORDER_CREATED)) {
                OrderId orderId = OrderId.of(orderEvent.orderRequest().orderId());
                sagaOrchestrator.onOrderCreated(orderId);
            }
        };
    }

    /**
     * Consume eventos de pago.
     */
    @Bean
    public Consumer<PaymentEvent> paymentEventConsumer() {
        return paymentEvent -> {
            log.info("Kafka: Received PaymentEvent with status: {}", paymentEvent.status());

            if (paymentEvent.paymentRequest() == null || paymentEvent.paymentRequest().orderId() == null) {
                log.warn("Kafka: PaymentEvent received with null order ID, ignoring");
                return;
            }

            OrderId orderId = OrderId.of(paymentEvent.paymentRequest().orderId());

            if (paymentEvent.status().equals(PaymentStatus.PAYMENT_COMPLETED)) {
                sagaOrchestrator.onPaymentCompleted(orderId);
            } else if (paymentEvent.status().equals(PaymentStatus.PAYMENT_FAILED)) {
                sagaOrchestrator.onPaymentFailed(orderId);
            }
        };
    }

    /**
     * Consume eventos de inventario.
     */
    @Bean
    public Consumer<InventoryEvent> inventoryEventConsumer() {
        return inventoryEvent -> {
            log.info("Kafka: Received InventoryEvent with status: {}", inventoryEvent.status());

            if (inventoryEvent.inventoryRequest() == null || inventoryEvent.inventoryRequest().orderId() == null) {
                log.warn("Kafka: InventoryEvent received with null order ID, ignoring");
                return;
            }

            OrderId orderId = OrderId.of(inventoryEvent.inventoryRequest().orderId());

            if (inventoryEvent.status().equals(InventoryStatus.INVENTORY_RESERVED)) {
                sagaOrchestrator.onInventoryReserved(orderId);
            } else if (inventoryEvent.status().equals(InventoryStatus.INVENTORY_REJECTED)) {
                sagaOrchestrator.onInventoryRejected(orderId);
            }
        };
    }
}
