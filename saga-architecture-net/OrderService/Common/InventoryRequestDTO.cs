namespace Saga.OrderService.Common;

public record InventoryRequestDTO(int UserId, int ProductId, Guid OrderId);
