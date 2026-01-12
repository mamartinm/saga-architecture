package com.example.saga.order.domain.model;

import com.example.saga.order.domain.event.DomainEvent;
import com.example.saga.order.domain.event.OrderCreatedDomainEvent;
import com.example.saga.order.domain.event.OrderStatusChangedDomainEvent;
import com.example.saga.order.domain.exception.InvalidOrderStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitarios del dominio - NO requieren Spring.
 * Validan la lógica de negocio encapsulada en el Aggregate Root.
 */
class OrderTest {

    @Nested
    @DisplayName("Creación de Orden")
    class OrderCreation {

        @Test
        @DisplayName("Debe crear orden con estado CREATED")
        void shouldCreateOrderWithCreatedStatus() {
            // When
            Order order = Order.create(
                    UserId.of(1),
                    ProductId.of(101),
                    Money.of(100.0));

            // Then
            assertThat(order.getId()).isNotNull();
            assertThat(order.getUserId().value()).isEqualTo(1);
            assertThat(order.getProductId().value()).isEqualTo(101);
            assertThat(order.getPrice().toDouble()).isEqualTo(100.0);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        }

        @Test
        @DisplayName("Debe generar evento OrderCreatedDomainEvent al crear")
        void shouldGenerateOrderCreatedEventOnCreation() {
            // When
            Order order = Order.create(
                    UserId.of(1),
                    ProductId.of(101),
                    Money.of(100.0));

            // Then
            List<DomainEvent> events = order.getDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(OrderCreatedDomainEvent.class);

            OrderCreatedDomainEvent event = (OrderCreatedDomainEvent) events.get(0);
            assertThat(event.orderId()).isEqualTo(order.getId());
            assertThat(event.userId()).isEqualTo(order.getUserId());
            assertThat(event.amount()).isEqualTo(order.getPrice());
        }

        @Test
        @DisplayName("Debe limpiar eventos después de publicarlos")
        void shouldClearEventsAfterPublishing() {
            // Given
            Order order = Order.create(UserId.of(1), ProductId.of(101), Money.of(100.0));
            assertThat(order.getDomainEvents()).isNotEmpty();

            // When
            order.clearDomainEvents();

            // Then
            assertThat(order.getDomainEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Transiciones de Estado")
    class StateTransitions {

        @Test
        @DisplayName("Debe transicionar correctamente en happy path")
        void shouldTransitionThroughHappyPath() {
            // Given
            Order order = Order.create(UserId.of(1), ProductId.of(101), Money.of(100.0));
            order.clearDomainEvents(); // Limpiar evento de creación

            // When - Simular flujo de saga
            order.markPaymentPending();
            order.confirmPayment();
            order.markInventoryPending();
            order.confirmInventory();
            order.complete();

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);

            // Verificar que se generaron eventos de cambio de estado
            List<DomainEvent> events = order.getDomainEvents();
            assertThat(events).hasSize(5);
            assertThat(events).allMatch(e -> e instanceof OrderStatusChangedDomainEvent);
        }

        @Test
        @DisplayName("Debe cancelar automáticamente cuando el pago falla")
        void shouldAutoCancelOnPaymentFailure() {
            // Given
            Order order = Order.create(UserId.of(1), ProductId.of(101), Money.of(100.0));
            order.clearDomainEvents();

            // When
            order.failPayment();

            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("No debe completar orden si no está en INVENTORY_RESERVED")
        void shouldNotCompleteIfNotReserved() {
            // Given
            Order order = Order.create(UserId.of(1), ProductId.of(101), Money.of(100.0));

            // Then
            assertThatThrownBy(order::complete)
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("complete");
        }

        @Test
        @DisplayName("No debe cancelar orden ya completada")
        void shouldNotCancelCompletedOrder() {
            // Given
            Order order = createCompletedOrder();

            // Then
            assertThatThrownBy(order::cancel)
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("cancel");
        }

        @Test
        @DisplayName("No debe modificar orden ya cancelada")
        void shouldNotModifyCancelledOrder() {
            // Given
            Order order = Order.create(UserId.of(1), ProductId.of(101), Money.of(100.0));
            order.cancel();

            // Then
            assertThatThrownBy(order::markPaymentPending)
                    .isInstanceOf(InvalidOrderStateException.class);
        }

        private Order createCompletedOrder() {
            Order order = Order.create(UserId.of(1), ProductId.of(101), Money.of(100.0));
            order.confirmPayment();
            order.confirmInventory();
            order.complete();
            return order;
        }
    }

    @Nested
    @DisplayName("Value Objects")
    class ValueObjectsTests {

        @Test
        @DisplayName("Money no debe aceptar valores negativos")
        void moneyShouldNotAcceptNegativeValues() {
            assertThatThrownBy(() -> Money.of(-10.0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negative");
        }

        @Test
        @DisplayName("UserId no debe aceptar valores no positivos")
        void userIdShouldNotAcceptNonPositiveValues() {
            assertThatThrownBy(() -> UserId.of(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> UserId.of(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Money debe soportar operaciones aritméticas")
        void moneyShouldSupportArithmetic() {
            Money m1 = Money.of(100.0);
            Money m2 = Money.of(50.0);

            assertThat(m1.add(m2).toDouble()).isEqualTo(150.0);
            assertThat(m1.subtract(m2).toDouble()).isEqualTo(50.0);
            assertThat(m1.isGreaterThan(m2)).isTrue();
        }
    }
}
