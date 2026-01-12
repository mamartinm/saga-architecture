namespace Saga.PaymentService.Common;

public record InventoryRequestDTO(int UserId, int ProductId, Guid OrderId);
