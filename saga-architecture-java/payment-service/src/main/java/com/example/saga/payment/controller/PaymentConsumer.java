package com.example.saga.payment.controller;

import com.example.saga.common.PaymentEvent;
import com.example.saga.common.PaymentRequestDTO;
import com.example.saga.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.function.Consumer;

@Controller
@Slf4j
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentService paymentService;
    private final StreamBridge streamBridge;

    @Bean
    public Consumer<PaymentRequestDTO> paymentRequestConsumer() {
        return paymentRequest -> {
            log.info("Payment Command Received for order: {}", paymentRequest.orderId());
            
            // Check if it's a new payment or a refund
            // Simple logic: if amount is 0 or it's a known refund logic (user asked for simpler demo)
            // But wait, OrderSagaOrchestrator sends 0.0 for refund? No, imports check.
            // Actually Orchestrator was sending 0.0 for refund.
            // Let's assume if amount is positive -> debit. If we needed true refund, we might need a flag or separate topic.
            // For now, let's process standard payments.
            
            if (paymentRequest.amount() == 0.0) {
                 // Amount 0 signals a Refund Command in this demo flow
                 paymentService.refundPayment(paymentRequest);
                 // We don't emit event for refund in this simplification, or we could emit PaymentRefunded
            } else {
                 PaymentEvent event = paymentService.processPayment(paymentRequest);
                 streamBridge.send("payment-events-out-0", event);
            }
        };
    }
}
