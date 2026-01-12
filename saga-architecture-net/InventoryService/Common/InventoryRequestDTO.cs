namespace Saga.InventoryService.Common;

public record InventoryRequestDTO(int UserId, int ProductId, Guid OrderId);
