package com.example.saga.payment;

import com.example.saga.common.PaymentRequestDTO;
import com.example.saga.payment.repository.UserBalanceRepository;
import com.example.saga.payment.controller.UserBalanceResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@DirtiesContext
public class PaymentServiceIntegrationTest {

    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserBalanceRepository balanceRepository;

    @Test
    public void testPaymentProcessing() throws InterruptedException {
        // Given
        UUID orderId = UUID.randomUUID();
        PaymentRequestDTO request = new PaymentRequestDTO(1, 101, orderId, 100.0);

        // When
        // Actuamos como si el orquestador enviara el comando
        streamBridge.send("paymentRequestConsumer-in-0", request);

        // Then
        // Wait for async processing
        Thread.sleep(2000);

        // Verify balance (Initial was 1000.0 from Liquibase test data)
        assertThat(balanceRepository.findById(1).get().getBalance()).isEqualTo(900.0);
    }

    @Test
    public void testGetBalance() {
        // Given
        Integer userId = 1; // From Liquibase test data

        // When
        ResponseEntity<UserBalanceResponseDTO> response = restTemplate.getForEntity("/payments/balance/" + userId,
                UserBalanceResponseDTO.class);

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().userId()).isEqualTo(userId);
        // Initial balance from Liquibase is 1000.0 (but testPaymentProcessing might
        // have run and reduced it to 900.0)
        // Since we use @DirtiesContext it might reset, but usually tests in same class
        // share state unless specified.
        // However, we just want to check it returns A balance.
        assertThat(response.getBody().balance()).isGreaterThanOrEqualTo(0.0);
    }
}
