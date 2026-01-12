package com.example.saga.order.infrastructure.adapter.input.rest;

import com.example.saga.order.domain.model.Order;
import com.example.saga.order.domain.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO para la respuesta REST de una orden.
 * Representa la vista externa de una orden.
 */
@Schema(description = "Respuesta con los datos de una orden")
public record OrderResponse(
        @Schema(description = "ID único de la orden") UUID id,

        @Schema(description = "ID del usuario") Integer userId,

        @Schema(description = "ID del producto") Integer productId,

        @Schema(description = "Precio de la orden") Double price,

        @Schema(description = "Estado actual de la orden") OrderStatus status,

        @Schema(description = "Fecha de creación") Instant createdAt,

        @Schema(description = "Fecha de última actualización") Instant updatedAt) {
    /**
     * Crea un OrderResponse a partir de una entidad de dominio.
     */
    public static OrderResponse fromDomain(Order order) {
        return new OrderResponse(
                order.getId().value(),
                order.getUserId().value(),
                order.getProductId().value(),
                order.getPrice().toDouble(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
