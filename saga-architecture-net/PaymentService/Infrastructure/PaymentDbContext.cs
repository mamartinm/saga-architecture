using Microsoft.EntityFrameworkCore;
using Saga.PaymentService.Domain;

namespace Saga.PaymentService.Infrastructure;

public class PaymentDbContext : DbContext
{
    public PaymentDbContext(DbContextOptions<PaymentDbContext> options) : base(options) { }
    
    public DbSet<UserBalance> Balances { get; set; }
    public DbSet<PaymentTransaction> Transactions { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<UserBalance>().ToTable("user_balance").HasKey(u => u.UserId);
        modelBuilder.Entity<PaymentTransaction>().ToTable("payment_transaction").HasKey(t => t.OrderId);
    }
}
