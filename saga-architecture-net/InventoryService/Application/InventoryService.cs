using Saga.InventoryService.Common;
using Saga.InventoryService.Domain;

namespace Saga.InventoryService.Application;

public interface IProductRepository
{
    Task<Product?> GetProduct(int productId);
    Task UpdateProduct(Product product);
}

public class InventoryService(IProductRepository repository, IKafkaProducer producer)
{
    public async Task<InventoryEvent> DeductInventory(InventoryRequestDTO request)
    {
        var product = await repository.GetProduct(request.ProductId);
        if (product != null && product.AvailableStock > 0)
        {
            product.AvailableStock--;
            await repository.UpdateProduct(product);
            var evt = new InventoryEvent(request, InventoryStatus.INVENTORY_RESERVED);
            await producer.ProduceAsync("inventory-events", evt);
            return evt;
        }
        else
        {
            var evt = new InventoryEvent(request, InventoryStatus.INVENTORY_REJECTED);
            await producer.ProduceAsync("inventory-events", evt);
            return evt;
        }
    }
}

public interface IKafkaProducer
{
    Task ProduceAsync<T>(string topic, T message);
}
