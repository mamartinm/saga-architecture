using Microsoft.EntityFrameworkCore;
using Saga.PaymentService.Application;
using Saga.PaymentService.Domain;

namespace Saga.PaymentService.Infrastructure;

public class BalanceRepository(PaymentDbContext context) : IBalanceRepository
{
    public async Task<UserBalance?> GetBalance(int userId) => await context.Balances.FindAsync(userId);
    public async Task UpdateBalance(UserBalance balance)
    {
        context.Balances.Update(balance);
        await context.SaveChangesAsync();
    }
}

public class PaymentTransactionRepository(PaymentDbContext context) : IPaymentTransactionRepository
{
    public async Task SaveTransaction(PaymentTransaction transaction)
    {
        context.Transactions.Add(transaction);
        await context.SaveChangesAsync();
    }
    public async Task<PaymentTransaction?> GetTransaction(Guid orderId) => await context.Transactions.FindAsync(orderId);
    public async Task UpdateTransaction(PaymentTransaction transaction)
    {
        context.Transactions.Update(transaction);
        await context.SaveChangesAsync();
    }
}
