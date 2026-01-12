package com.example.saga.order.infrastructure.adapter.input.rest;

import com.example.saga.order.domain.model.Money;
import com.example.saga.order.domain.model.Order;
import com.example.saga.order.domain.model.OrderId;
import com.example.saga.order.domain.model.ProductId;
import com.example.saga.order.domain.model.UserId;
import com.example.saga.order.domain.port.input.CreateOrderUseCase.CreateOrderCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderRestMapper {

    @Mapping(target = "userId", source = "userId", qualifiedByName = "mapUserId")
    @Mapping(target = "productId", source = "productId", qualifiedByName = "mapProductId")
    @Mapping(target = "amount", source = "amount", qualifiedByName = "mapMoney")
    CreateOrderCommand toCommand(CreateOrderRequest request);

    @Mapping(target = "id", source = "id", qualifiedByName = "mapOrderId")
    @Mapping(target = "userId", source = "userId", qualifiedByName = "mapUserIdToInt")
    @Mapping(target = "productId", source = "productId", qualifiedByName = "mapProductIdToInt")
    @Mapping(target = "price", source = "price", qualifiedByName = "mapMoneyToDouble")
    @Mapping(target = "status", source = "status")
    OrderResponse toResponse(Order order);

    @Named("mapUserId")
    default UserId mapUserId(Integer value) {
        return value != null ? UserId.of(value) : null;
    }

    @Named("mapProductId")
    default ProductId mapProductId(Integer value) {
        return value != null ? ProductId.of(value) : null;
    }

    @Named("mapMoney")
    default Money mapMoney(Double value) {
        return value != null ? Money.of(value) : null;
    }

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
