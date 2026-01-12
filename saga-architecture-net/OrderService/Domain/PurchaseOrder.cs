using Saga.OrderService.Common;

namespace Saga.OrderService.Domain;

public class PurchaseOrder
{
    public Guid Id { get; set; }
    public int UserId { get; set; }
    public int ProductId { get; set; }
    public double Price { get; set; }
    public OrderStatus OrderStatus { get; set; }
}
