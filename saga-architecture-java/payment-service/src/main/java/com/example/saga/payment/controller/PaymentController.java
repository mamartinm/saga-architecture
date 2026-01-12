package com.example.saga.payment.controller;

import com.example.saga.payment.entity.UserBalance;
import com.example.saga.payment.mapper.PaymentMapper;
import com.example.saga.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Payment API", description = "Endpoints para la gestión de pagos y saldos")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @GetMapping("/balance/{userId}")
    @Operation(summary = "Obtener Saldo", description = "Retorna el saldo actual de un usuario.")
    public ResponseEntity<UserBalanceResponseDTO> getUserBalance(@PathVariable Integer userId) {
        log.info("Consultando saldo para userId: {}", userId);
        UserBalance balance = paymentService.getUserBalance(userId);
        if (balance == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(paymentMapper.toBalanceDto(balance));
    }
}
