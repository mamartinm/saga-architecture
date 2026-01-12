package com.example.saga.payment.mapper;

import com.example.saga.common.PaymentRequestDTO;
import com.example.saga.payment.controller.UserBalanceResponseDTO;
import com.example.saga.payment.entity.PaymentTransaction;
import com.example.saga.payment.entity.UserBalance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "status", constant = "PENDING")
    PaymentTransaction toTransaction(PaymentRequestDTO requestDTO);

    UserBalanceResponseDTO toBalanceDto(UserBalance entity);
}
