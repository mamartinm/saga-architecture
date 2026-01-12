using Saga.OrderService.Domain;

namespace Saga.OrderService.Domain;

public interface IOrderRepository
{
    Task<PurchaseOrder> CreateOrder(PurchaseOrder order);
    Task<PurchaseOrder?> GetOrderById(Guid orderId);
    Task<PurchaseOrder> UpdateOrder(PurchaseOrder order);
    Task<IEnumerable<PurchaseOrder>> GetAllOrders();
}
