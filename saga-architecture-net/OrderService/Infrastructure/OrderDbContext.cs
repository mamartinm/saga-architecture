using Microsoft.EntityFrameworkCore;
using Saga.OrderService.Domain;

namespace Saga.OrderService.Infrastructure;

public class OrderDbContext : DbContext
{
    public OrderDbContext(DbContextOptions<OrderDbContext> options) : base(options) { }
    
    public DbSet<PurchaseOrder> Orders { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<PurchaseOrder>().ToTable("orders");
        modelBuilder.Entity<PurchaseOrder>().HasKey(o => o.Id);
    }
}
