package com.example.saga.common;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


public record OrderRequestDTO(
                @Schema(description = "ID del usuario", example = "101") @NotNull(message = "User ID cannot be null") Integer userId,
                @Schema(description = "ID del producto", example = "202") @NotNull(message = "Product ID cannot be null") Integer productId,
                @Schema(description = "Monto de la orden", example = "150.00", minimum = "0") @Min(value = 0, message = "Amount must be positive") Double amount,
                @Schema(hidden = true) UUID orderId) {
}
