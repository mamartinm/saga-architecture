using Microsoft.EntityFrameworkCore;
using Saga.InventoryService.Application;
using Saga.InventoryService.Domain;

namespace Saga.InventoryService.Infrastructure;

public class ProductRepository(InventoryDbContext context) : IProductRepository
{
    public async Task<Product?> GetProduct(int productId) => await context.Products.FindAsync(productId);
    public async Task UpdateProduct(Product product)
    {
        context.Products.Update(product);
        await context.SaveChangesAsync();
    }
}
