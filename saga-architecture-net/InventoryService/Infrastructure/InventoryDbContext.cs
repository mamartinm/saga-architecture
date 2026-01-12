using Microsoft.EntityFrameworkCore;
using Saga.InventoryService.Domain;

namespace Saga.InventoryService.Infrastructure;

public class InventoryDbContext : DbContext
{
    public InventoryDbContext(DbContextOptions<InventoryDbContext> options) : base(options) { }
    
    public DbSet<Product> Products { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Product>().ToTable("product_inventory").HasKey(p => p.ProductId);
    }
}
