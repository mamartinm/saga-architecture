package com.example.saga.order.infrastructure.adapter.output.messaging;

import com.example.saga.order.domain.port.output.PaymentCommandSender;
import com.example.saga.common.PaymentRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/**
 * Adaptador Kafka para enviar comandos al servicio de pagos.
 * Usa MapStruct para mapear comandos de dominio a DTOs de integración.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaPaymentCommandSender implements PaymentCommandSender {

    private static final String PAYMENT_COMMANDS_OUT = "payment-commands-out";

    private final StreamBridge streamBridge;
    private final OrderIntegrationMapper mapper;

    @Override
    public void sendProcessPaymentCommand(ProcessPaymentCommand command) {
        log.info("Sending process payment command for order: {}", command.orderId());

        PaymentRequestDTO paymentRequest = mapper.toPaymentRequest(command);

        streamBridge.send(PAYMENT_COMMANDS_OUT, paymentRequest);
        log.debug("Payment command sent successfully");
    }

    @Override
    public void sendRefundPaymentCommand(RefundPaymentCommand command) {
        log.info("Sending refund payment command for order: {}", command.orderId());

        PaymentRequestDTO refundRequest = mapper.toRefundRequest(command);

        streamBridge.send(PAYMENT_COMMANDS_OUT, refundRequest);
        log.debug("Refund command sent successfully");
    }
}
