namespace Saga.InventoryService.Common;

public record PaymentRequestDTO(int UserId, Guid OrderId, double Amount);
