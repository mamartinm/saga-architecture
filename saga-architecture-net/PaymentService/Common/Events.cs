namespace Saga.PaymentService.Common;

public record OrderEvent(OrderRequestDTO OrderRequest, OrderStatus Status);
public record PaymentEvent(PaymentRequestDTO PaymentRequest, PaymentStatus Status);
public record InventoryEvent(InventoryRequestDTO InventoryRequest, InventoryStatus Status);
