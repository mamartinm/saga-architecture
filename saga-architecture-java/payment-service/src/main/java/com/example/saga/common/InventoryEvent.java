package com.example.saga.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Evento de Inventario para la Saga")
public record InventoryEvent(
        @Schema(description = "DTO de la petición de inventario original") InventoryRequestDTO inventoryRequest,
        @Schema(description = "Estado actual de la reserva") InventoryStatus status) {
}
