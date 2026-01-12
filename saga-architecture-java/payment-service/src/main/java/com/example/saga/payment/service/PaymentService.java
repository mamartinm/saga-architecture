package com.example.saga.payment.service;

import com.example.saga.common.PaymentRequestDTO;
import com.example.saga.common.PaymentEvent;
import com.example.saga.common.PaymentStatus;
import com.example.saga.payment.entity.PaymentTransaction;
import com.example.saga.payment.entity.UserBalance;
import com.example.saga.payment.repository.PaymentTransactionRepository;
import com.example.saga.payment.repository.UserBalanceRepository;
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
public class PaymentService {

    private final UserBalanceRepository balanceRepository;
    private final PaymentTransactionRepository transactionRepository;

    @Transactional
    public PaymentEvent processPayment(PaymentRequestDTO paymentRequest) {
        log.info("Processing payment for order: {}", paymentRequest.orderId());

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderId(paymentRequest.orderId());
        transaction.setUserId(paymentRequest.userId());
        transaction.setAmount(paymentRequest.amount());
        transaction.setProductId(paymentRequest.productId());

        return balanceRepository.findById(paymentRequest.userId())
                .filter(ub -> ub.getBalance() >= paymentRequest.amount())
                .map(ub -> {
                    ub.setBalance(ub.getBalance() - paymentRequest.amount());
                    balanceRepository.save(ub);
                    transaction.setStatus("APPROVED");
                    transactionRepository.save(transaction);
                    return new PaymentEvent(paymentRequest, PaymentStatus.PAYMENT_COMPLETED);
                })
                .orElseGet(() -> {
                    transaction.setStatus("REJECTED");
                    transactionRepository.save(transaction);
                    return new PaymentEvent(paymentRequest, PaymentStatus.PAYMENT_FAILED);
                });
    }

    @Transactional
    public void refundPayment(PaymentRequestDTO paymentRequest) {
        log.info("Refunding payment for order: {}", paymentRequest.orderId());

        // Find successful transaction to refund
        transactionRepository.findById(paymentRequest.orderId()).ifPresent(txn -> {
            balanceRepository.findById(paymentRequest.userId()).ifPresent(ub -> {
                ub.setBalance(ub.getBalance() + txn.getAmount());
                balanceRepository.save(ub);
                log.info("Refunded {} to user {}", txn.getAmount(), paymentRequest.userId());
            });
            txn.setStatus("REFUNDED");
            transactionRepository.save(txn);
        });
    }

    @Transactional(readOnly = true)
    public UserBalance getUserBalance(Integer userId) {
        return balanceRepository.findById(userId).orElse(null);
    }
}
