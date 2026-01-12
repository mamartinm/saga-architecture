package com.example.saga.inventory;

import com.example.saga.common.InventoryRequestDTO;
import com.example.saga.inventory.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9093", "port=9093" })
@DirtiesContext
public class InventoryServiceIntegrationTest {

    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    private ProductRepository inventoryRepository;

    @Test
    public void testInventoryReservation() throws InterruptedException {
        // Given
        UUID orderId = UUID.randomUUID();
        InventoryRequestDTO request = new InventoryRequestDTO(1, 101, orderId);
        
        // When
        streamBridge.send("inventoryRequestConsumer-in-0", request);
        
        // Then
        // Wait for async processing
        Thread.sleep(2000);
        
        // Verify stock (Initial was 10 from Liquibase test data)
        assertThat(inventoryRepository.findById(101).get().getAvailableStock()).isEqualTo(9);
    }
}
