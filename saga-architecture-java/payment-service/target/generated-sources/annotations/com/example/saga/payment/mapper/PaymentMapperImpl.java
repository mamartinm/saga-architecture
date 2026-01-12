package com.example.saga.payment.mapper;

import com.example.saga.common.PaymentRequestDTO;
import com.example.saga.payment.controller.UserBalanceResponseDTO;
import com.example.saga.payment.entity.PaymentTransaction;
import com.example.saga.payment.entity.UserBalance;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-09T11:44:26+0100",
    comments = "version: 1.6.2, compiler: Eclipse JDT (IDE) 3.45.0.v20260101-2150, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class PaymentMapperImpl implements PaymentMapper {

    @Override
    public PaymentTransaction toTransaction(PaymentRequestDTO requestDTO) {
        if ( requestDTO == null ) {
            return null;
        }

        PaymentTransaction paymentTransaction = new PaymentTransaction();

        paymentTransaction.setAmount( requestDTO.amount() );
        paymentTransaction.setOrderId( requestDTO.orderId() );
        paymentTransaction.setProductId( requestDTO.productId() );
        paymentTransaction.setUserId( requestDTO.userId() );

        paymentTransaction.setStatus( "PENDING" );

        return paymentTransaction;
    }

    @Override
    public UserBalanceResponseDTO toBalanceDto(UserBalance entity) {
        if ( entity == null ) {
            return null;
        }

        Integer userId = null;
        Double balance = null;

        userId = entity.getUserId();
        balance = entity.getBalance();

        UserBalanceResponseDTO userBalanceResponseDTO = new UserBalanceResponseDTO( userId, balance );

        return userBalanceResponseDTO;
    }
}
