using System.ComponentModel.DataAnnotations;

namespace Saga.InventoryService.Common;

public record OrderRequestDTO(
    [Required] int UserId,
    [Required] int ProductId,
    [Range(0, double.MaxValue)] double Amount,
    Guid? OrderId
);
