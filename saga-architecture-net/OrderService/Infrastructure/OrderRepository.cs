using Microsoft.EntityFrameworkCore;
using Saga.OrderService.Domain;

namespace Saga.OrderService.Infrastructure;

public class OrderRepository(OrderDbContext context) : IOrderRepository
{
    public async Task<PurchaseOrder> CreateOrder(PurchaseOrder order)
    {
        context.Orders.Add(order);
        await context.SaveChangesAsync();
        return order;
    }

    public async Task<PurchaseOrder?> GetOrderById(Guid orderId) => await context.Orders.FindAsync(orderId);

    public async Task<PurchaseOrder> UpdateOrder(PurchaseOrder order)
    {
        context.Orders.Update(order);
        await context.SaveChangesAsync();
        return order;
    }

    public async Task<IEnumerable<PurchaseOrder>> GetAllOrders() => await context.Orders.ToListAsync();
}
