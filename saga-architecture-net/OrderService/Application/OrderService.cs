using Saga.OrderService.Common;
using Saga.OrderService.Domain;

namespace Saga.OrderService.Application;

public class OrderAppService(IOrderRepository repository, IKafkaProducer producer)
{
    public async Task<PurchaseOrder> CreateOrder(PurchaseOrder order)
    {
        order.Id = Guid.NewGuid();
        order.OrderStatus = OrderStatus.ORDER_CREATED;
        var savedOrder = await repository.CreateOrder(order);
        
        // Emit Order Created Event
        var orderEvent = new OrderEvent(new OrderRequestDTO(savedOrder.UserId, savedOrder.ProductId, savedOrder.Price, savedOrder.Id), OrderStatus.ORDER_CREATED);
        await producer.ProduceAsync("order-events", orderEvent);
        
        return savedOrder;
    }

    public async Task<IEnumerable<PurchaseOrder>> GetAllOrders() => await repository.GetAllOrders();
}

public interface IKafkaProducer
{
    Task ProduceAsync<T>(string topic, T message);
}
