package com.example.saga.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Evento de Pago para la Saga")
public record PaymentEvent(
        @Schema(description = "DTO de la petición de pago original") PaymentRequestDTO paymentRequest,
        @Schema(description = "Estado actual del pago") PaymentStatus status) {
}
