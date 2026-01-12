package com.example.saga.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Evento de Pedido para la Saga")
public record OrderEvent(
        @Schema(description = "DTO de la petición original") OrderRequestDTO orderRequest,
        @Schema(description = "Estado actual del pedido") OrderStatus status) {
}
