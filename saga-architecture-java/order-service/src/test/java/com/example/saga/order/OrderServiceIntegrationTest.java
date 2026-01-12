package com.example.saga.order;

import com.example.saga.order.infrastructure.adapter.input.rest.CreateOrderRequest;
import com.example.saga.order.infrastructure.adapter.input.rest.OrderResponse;
import com.example.saga.order.infrastructure.adapter.output.persistence.OrderJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9094", "port=9094" })
@DirtiesContext
public class OrderServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderJpaRepository orderRepository;

    @Test
    public void testOrderCreation() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(1, 101, 100.0);

        // When
        ResponseEntity<OrderResponse> response = restTemplate.postForEntity("/orders", request, OrderResponse.class);

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().userId()).isEqualTo(1);
        assertThat(response.getBody().productId()).isEqualTo(101);
        assertThat(response.getBody().price()).isEqualTo(100.0);

        // Verify order saved in DB
        assertThat(orderRepository.findAll()).isNotEmpty();
    }
}
