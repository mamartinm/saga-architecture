package com.example.saga.order.infrastructure.adapter.input.rest;

import com.example.saga.order.domain.model.Order;
import com.example.saga.order.domain.model.OrderId;
import com.example.saga.order.domain.port.input.CreateOrderUseCase;
import com.example.saga.order.domain.port.input.CreateOrderUseCase.CreateOrderCommand;
import com.example.saga.order.domain.port.input.GetOrderUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Adaptador REST de entrada para la gestión de órdenes.
 * Convierte peticiones HTTP a comandos de dominio usando MapStruct.
 */
@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Order API", description = "Endpoints para la gestión de pedidos y orquestación SAGA")
@Slf4j
@RequiredArgsConstructor
public class OrderRestController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final OrderRestMapper mapper;

    @PostMapping
    @Operation(summary = "Crear Pedido", description = "Inicia el proceso de creación de pedido disparando la Saga Orquestada.")
    @ApiResponse(responseCode = "200", description = "Pedido aceptado y en proceso")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid CreateOrderRequest request) {
        log.info("REST: Received create order request for userId: {}", request.userId());

        // Convertir request REST a comando de dominio usando mapper
        CreateOrderCommand command = mapper.toCommand(request);

        // Ejecutar caso de uso
        Order order = createOrderUseCase.execute(command);

        // Convertir respuesta de dominio a DTO REST usando mapper
        OrderResponse response = mapper.toResponse(order);

        log.info("REST: Order created successfully with ID: {}", response.id());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Obtener Pedido", description = "Obtiene los detalles de un pedido por su ID.")
    @ApiResponse(responseCode = "200", description = "Pedido encontrado")
    @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        log.info("REST: Received get order request for orderId: {}", orderId);

        return getOrderUseCase.execute(OrderId.of(orderId))
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
