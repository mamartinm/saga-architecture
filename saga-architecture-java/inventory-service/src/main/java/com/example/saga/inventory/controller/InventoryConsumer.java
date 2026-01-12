package com.example.saga.inventory.controller;

import com.example.saga.common.InventoryEvent;
import com.example.saga.common.InventoryRequestDTO;
import com.example.saga.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.function.Consumer;

@Controller
@Slf4j
@RequiredArgsConstructor
public class InventoryConsumer {

    private final InventoryService inventoryService;
    private final StreamBridge streamBridge;

    @Bean
    public Consumer<InventoryRequestDTO> inventoryRequestConsumer() {
        return inventoryRequest -> {
            log.info("Inventory Command Received for order: {}", inventoryRequest.orderId());
            InventoryEvent event = inventoryService.deductInventory(inventoryRequest);
            streamBridge.send("inventory-events-out-0", event);
        };
    }
}
