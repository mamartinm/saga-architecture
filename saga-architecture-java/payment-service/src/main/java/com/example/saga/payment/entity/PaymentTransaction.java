package com.example.saga.payment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payment_transaction")
public class PaymentTransaction {
    @Id
    private UUID orderId;
    private Integer userId;
    private Double amount;
    private Integer productId;
    private String status; // APPROVED, REJECTED, REFUNDED
}
