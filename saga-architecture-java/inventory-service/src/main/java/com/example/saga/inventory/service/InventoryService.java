package com.example.saga.inventory.service;

import com.example.saga.common.InventoryRequestDTO;
import com.example.saga.common.InventoryEvent;
import com.example.saga.common.InventoryStatus;
import com.example.saga.inventory.entity.Product;
import com.example.saga.inventory.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;



    @Transactional
    public InventoryEvent deductInventory(InventoryRequestDTO inventoryRequest) {
        log.info("Processing inventory for order: {}", inventoryRequest.orderId());

        return productRepository.findById(inventoryRequest.productId())
                .filter(p -> p.getAvailableStock() > 0)
                .map(p -> {
                    p.setAvailableStock(p.getAvailableStock() - 1); // Deduct 1 for simplicity or add quantity to DTO later
                    productRepository.save(p);
                    return new InventoryEvent(inventoryRequest, InventoryStatus.INVENTORY_RESERVED);
                })
                .orElseGet(() -> {
                    log.warn("Inventory rejected for product: {}", inventoryRequest.productId());
                    return new InventoryEvent(inventoryRequest, InventoryStatus.INVENTORY_REJECTED);
                });
    }

    @Transactional
    public void addInventory(InventoryRequestDTO inventoryRequest) {
        // Compensation logic if needed (e.g. if Order Cancelled after inventory reserved but before completion)
        // For this saga flow, inventory is the last step before completion, so we might not need compensation 
        // unless there's a step AFTER inventory.
        // But if payment fails we might need to rollback inventory? 
        // Actually the flow is Order -> Payment -> Inventory. If Inventory fails, we refund payment.
        // If Inventory succeeds, we just complete order.
        // So we don't strictly need "addInventory" triggered by Saga unless we add Shipping Service later.
        // But good to have for completeness or manual cancellations.
        productRepository.findById(inventoryRequest.productId()).ifPresent(p -> {
            p.setAvailableStock(p.getAvailableStock() + 1);
            productRepository.save(p);
        });
    }
}
