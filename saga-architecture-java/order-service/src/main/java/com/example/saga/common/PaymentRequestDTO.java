package com.example.saga.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "DTO para Solicitud de Pago")
public record PaymentRequestDTO(
                @Schema(description = "ID del usuario", example = "101") Integer userId,
                @Schema(description = "ID del producto", example = "202") Integer productId,
                @Schema(description = "ID del pedido (UUID)") UUID orderId,
                @Schema(description = "Monto a cobrar", example = "100.0") Double amount) {
}
