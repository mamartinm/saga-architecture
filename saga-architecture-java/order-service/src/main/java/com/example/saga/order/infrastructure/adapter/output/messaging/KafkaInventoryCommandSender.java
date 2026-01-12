package com.example.saga.order.infrastructure.adapter.output.messaging;

import com.example.saga.order.domain.port.output.InventoryCommandSender;
import com.example.saga.common.InventoryRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/**
 * Adaptador Kafka para enviar comandos al servicio de inventario.
 * Usa MapStruct para mapear comandos de dominio a DTOs de integración.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaInventoryCommandSender implements InventoryCommandSender {

    private static final String INVENTORY_COMMANDS_OUT = "inventory-commands-out";

    private final StreamBridge streamBridge;
    private final OrderIntegrationMapper mapper;

    @Override
    public void sendReserveInventoryCommand(ReserveInventoryCommand command) {
        log.info("Sending reserve inventory command for order: {}", command.orderId());

        InventoryRequestDTO inventoryRequest = mapper.toInventoryRequest(command);

        streamBridge.send(INVENTORY_COMMANDS_OUT, inventoryRequest);
        log.debug("Inventory reservation command sent successfully");
    }

    @Override
    public void sendReleaseInventoryCommand(ReleaseInventoryCommand command) {
        log.info("Sending release inventory command for order: {}", command.orderId());

        // Para liberar inventario, podríamos usar un topic diferente
        // Por ahora usamos el mismo con una señal especial
        InventoryRequestDTO releaseRequest = new InventoryRequestDTO(
                0, // userId no relevante
                command.productId().value(),
                command.orderId().value());

        // En un caso real, enviaríamos a un topic específico como "inventory-release"
        streamBridge.send(INVENTORY_COMMANDS_OUT, releaseRequest);
        log.debug("Inventory release command sent successfully");
    }
}
