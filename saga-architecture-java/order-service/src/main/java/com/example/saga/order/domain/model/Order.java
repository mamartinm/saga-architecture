package com.example.saga.order.domain.model;

import com.example.saga.order.domain.event.DomainEvent;
import com.example.saga.order.domain.event.OrderCreatedDomainEvent;
import com.example.saga.order.domain.event.OrderStatusChangedDomainEvent;
import com.example.saga.order.domain.exception.InvalidOrderStateException;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root que representa una Orden de compra.
 * 
 * Esta entidad de dominio:
 * - Es inmutable en sus propiedades de identidad (id, userId, productId)
 * - Encapsula la lógica de negocio para transiciones de estado
 * - Genera eventos de dominio cuando ocurren cambios significativos
 * - NO tiene dependencias de infraestructura (sin anotaciones JPA)
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Order {

    @EqualsAndHashCode.Include
    @ToString.Include
    private final OrderId id;

    @ToString.Include
    private final UserId userId;

    @ToString.Include
    private final ProductId productId;

    @ToString.Include
    private final Money price;

    @ToString.Include
    private OrderStatus status;

    private final Instant createdAt;
    private Instant updatedAt;

    // Lista de eventos de dominio pendientes de publicar
    @Getter(AccessLevel.NONE)
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Constructor privado - usar factory methods
    private Order(OrderId id, UserId userId, ProductId productId, Money price, OrderStatus status, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "OrderId cannot be null");
        this.userId = Objects.requireNonNull(userId, "UserId cannot be null");
        this.productId = Objects.requireNonNull(productId, "ProductId cannot be null");
        this.price = Objects.requireNonNull(price, "Price cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = createdAt;
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Crea una nueva orden con estado CREATED.
     * Genera un evento OrderCreatedDomainEvent.
     */
    public static Order create(UserId userId, ProductId productId, Money price) {
        OrderId orderId = OrderId.generate();
        Instant now = Instant.now();

        Order order = new Order(orderId, userId, productId, price, OrderStatus.CREATED, now);

        // Registrar evento de dominio
        order.registerEvent(new OrderCreatedDomainEvent(orderId, userId, productId, price));

        return order;
    }

    /**
     * Reconstruye una orden existente desde persistencia.
     * NO genera eventos de dominio (es solo reconstrucción).
     */
    public static Order reconstitute(OrderId id, UserId userId, ProductId productId,
            Money price, OrderStatus status, Instant createdAt, Instant updatedAt) {
        Order order = new Order(id, userId, productId, price, status, createdAt);
        order.updatedAt = updatedAt;
        return order;
    }

    // ==================== DOMAIN BEHAVIOR ====================

    /**
     * Marca el pago como pendiente.
     */
    public void markPaymentPending() {
        validateStateTransition(OrderStatus.PAYMENT_PENDING, "mark payment pending");
        changeStatus(OrderStatus.PAYMENT_PENDING);
    }

    /**
     * Confirma que el pago fue completado exitosamente.
     */
    public void confirmPayment() {
        if (status != OrderStatus.CREATED && status != OrderStatus.PAYMENT_PENDING) {
            throw new InvalidOrderStateException(status, "confirm payment");
        }
        changeStatus(OrderStatus.PAYMENT_COMPLETED);
    }

    /**
     * Marca el pago como fallido.
     */
    public void failPayment() {
        if (status != OrderStatus.CREATED && status != OrderStatus.PAYMENT_PENDING) {
            throw new InvalidOrderStateException(status, "fail payment");
        }
        changeStatus(OrderStatus.PAYMENT_FAILED);
        // Auto-cancelar cuando el pago falla
        changeStatus(OrderStatus.CANCELLED);
    }

    /**
     * Marca el inventario como pendiente de reserva.
     */
    public void markInventoryPending() {
        if (status != OrderStatus.PAYMENT_COMPLETED) {
            throw new InvalidOrderStateException(status, "mark inventory pending");
        }
        changeStatus(OrderStatus.INVENTORY_PENDING);
    }

    /**
     * Confirma que el inventario fue reservado exitosamente.
     */
    public void confirmInventory() {
        if (status != OrderStatus.PAYMENT_COMPLETED && status != OrderStatus.INVENTORY_PENDING) {
            throw new InvalidOrderStateException(status, "confirm inventory");
        }
        changeStatus(OrderStatus.INVENTORY_RESERVED);
    }

    /**
     * Marca la reserva de inventario como fallida.
     */
    public void failInventory() {
        if (status != OrderStatus.PAYMENT_COMPLETED && status != OrderStatus.INVENTORY_PENDING) {
            throw new InvalidOrderStateException(status, "fail inventory");
        }
        changeStatus(OrderStatus.INVENTORY_FAILED);
    }

    /**
     * Completa la orden exitosamente.
     * Solo es válido si el inventario fue reservado.
     */
    public void complete() {
        if (!status.canComplete()) {
            throw new InvalidOrderStateException(status, "complete");
        }
        changeStatus(OrderStatus.COMPLETED);
    }

    /**
     * Cancela la orden.
     * No se puede cancelar una orden ya completada o cancelada.
     */
    public void cancel() {
        if (!status.canCancel()) {
            throw new InvalidOrderStateException(status, "cancel");
        }
        changeStatus(OrderStatus.CANCELLED);
    }

    // ==================== PRIVATE HELPERS ====================

    private void changeStatus(OrderStatus newStatus) {
        OrderStatus previousStatus = this.status;
        this.status = newStatus;
        this.updatedAt = Instant.now();
        registerEvent(new OrderStatusChangedDomainEvent(this.id, previousStatus, newStatus));
    }

    private void validateStateTransition(OrderStatus targetStatus, String action) {
        if (status.isTerminal()) {
            throw new InvalidOrderStateException(status, action);
        }
    }

    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * Limpia los eventos de dominio después de publicarlos.
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    /**
     * Retorna una copia inmutable de los eventos de dominio pendientes.
     * (Getter manual porque tiene lógica especial)
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
}
