package com.example.saga.payment.controller;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con el saldo del usuario")
public record UserBalanceResponseDTO(
    @Schema(description = "ID del usuario", example = "1") Integer userId,
    @Schema(description = "Saldo disponible", example = "1000.0") Double balance
) {}
