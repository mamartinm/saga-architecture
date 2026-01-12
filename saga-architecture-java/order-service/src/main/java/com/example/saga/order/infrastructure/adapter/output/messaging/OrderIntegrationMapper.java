package com.example.saga.order.infrastructure.adapter.output.messaging;

import com.example.saga.common.InventoryRequestDTO;
import com.example.saga.common.OrderRequestDTO;
import com.example.saga.common.PaymentRequestDTO;
import com.example.saga.order.domain.event.OrderCreatedDomainEvent;
import com.example.saga.order.domain.model.Money;
import com.example.saga.order.domain.model.OrderId;
import com.example.saga.order.domain.model.ProductId;
import com.example.saga.order.domain.model.UserId;
import com.example.saga.order.domain.port.output.InventoryCommandSender.ReserveInventoryCommand;
import com.example.saga.order.domain.port.output.PaymentCommandSender.ProcessPaymentCommand;
import com.example.saga.order.domain.port.output.PaymentCommandSender.RefundPaymentCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderIntegrationMapper {

    // Domain Event -> Integration DTO
    @Mapping(target = "orderId", source = "orderId", qualifiedByName = "mapOrderId")
    @Mapping(target = "userId", source = "userId", qualifiedByName = "mapUserIdToInt")
    @Mapping(target = "productId", source = "productId", qualifiedByName = "mapProductIdToInt")
    @Mapping(target = "amount", source = "amount", qualifiedByName = "mapMoneyToDouble")
    OrderRequestDTO toOrderRequest(OrderCreatedDomainEvent event);

    // Commands -> Integration DTOs
    @Mapping(target = "orderId", source = "orderId", qualifiedByName = "mapOrderId")
    @Mapping(target = "userId", source = "userId", qualifiedByName = "mapUserIdToInt")
    @Mapping(target = "productId", source = "productId", qualifiedByName = "mapProductIdToInt")
    @Mapping(target = "amount", source = "amount", qualifiedByName = "mapMoneyToDouble")
    PaymentRequestDTO toPaymentRequest(ProcessPaymentCommand command);

    @Mapping(target = "orderId", source = "orderId", qualifiedByName = "mapOrderId")
    @Mapping(target = "userId", source = "userId", qualifiedByName = "mapUserIdToInt")
    @Mapping(target = "productId", constant = "0")
    @Mapping(target = "amount", constant = "0.0")
    PaymentRequestDTO toRefundRequest(RefundPaymentCommand command);

    @Mapping(target = "orderId", source = "orderId", qualifiedByName = "mapOrderId")
    @Mapping(target = "userId", source = "userId", qualifiedByName = "mapUserIdToInt")
    @Mapping(target = "productId", source = "productId", qualifiedByName = "mapProductIdToInt")
    InventoryRequestDTO toInventoryRequest(ReserveInventoryCommand command);

    // Mappers auxiliares
    @Named("mapOrderId")
    default UUID mapOrderId(OrderId value) {
        return value != null ? value.value() : null;
    }

    @Named("mapUserIdToInt")
    default Integer mapUserIdToInt(UserId value) {
        return value != null ? value.value() : null;
    }

    @Named("mapProductIdToInt")
    default Integer mapProductIdToInt(ProductId value) {
        return value != null ? value.value() : null;
    }

    @Named("mapMoneyToDouble")
    default Double mapMoneyToDouble(Money value) {
        return value != null ? value.toDouble() : null;
    }
}
