package com.example.saga.inventory.service;

import com.example.saga.common.InventoryEvent;
import com.example.saga.common.InventoryRequestDTO;
import com.example.saga.common.InventoryStatus;
import com.example.saga.inventory.entity.Product;
import com.example.saga.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                    log.warn("Inventory rejected for product: {} (Order ID: {})", inventoryRequest.productId(), inventoryRequest.orderId());
                    return new InventoryEvent(inventoryRequest, InventoryStatus.INVENTORY_REJECTED);
                });
    }

    @Transactional
    public void addInventory(InventoryRequestDTO inventoryRequest) {
        log.info("Compensating inventory for order: {} (Product ID: {})", inventoryRequest.orderId(), inventoryRequest.productId());
        
        // El comando de compensación (userId=0) viene del orquestador
        productRepository.findById(inventoryRequest.productId()).ifPresent(p -> {
            p.setAvailableStock(p.getAvailableStock() + 1);
            productRepository.save(p);
            log.info("Inventory added back for product {} (New stock: {})", inventoryRequest.productId(), p.getAvailableStock());
        });
    }
}
