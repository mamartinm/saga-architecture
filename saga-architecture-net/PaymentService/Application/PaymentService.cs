using Saga.PaymentService.Common;
using Saga.PaymentService.Domain;

namespace Saga.PaymentService.Application;

public interface IBalanceRepository
{
    Task<UserBalance?> GetBalance(int userId);
    Task UpdateBalance(UserBalance balance);
}

public interface IPaymentTransactionRepository
{
    Task SaveTransaction(PaymentTransaction transaction);
    Task<PaymentTransaction?> GetTransaction(Guid orderId);
    Task UpdateTransaction(PaymentTransaction transaction);
}

public class PaymentService(IBalanceRepository balanceRepo, IPaymentTransactionRepository txnRepo, IKafkaProducer producer)
{
    public async Task<PaymentEvent> ProcessPayment(PaymentRequestDTO request)
    {
        var balance = await balanceRepo.GetBalance(request.UserId);
        var txn = new PaymentTransaction { OrderId = request.OrderId, UserId = request.UserId, Amount = request.Amount };

        if (balance != null && balance.Balance >= request.Amount)
        {
            balance.Balance -= request.Amount;
            await balanceRepo.UpdateBalance(balance);
            txn.Status = "APPROVED";
            await txnRepo.SaveTransaction(txn);
            var evt = new PaymentEvent(request, PaymentStatus.PAYMENT_COMPLETED);
            await producer.ProduceAsync("payment-events", evt);
            return evt;
        }
        else
        {
            txn.Status = "REJECTED";
            await txnRepo.SaveTransaction(txn);
            var evt = new PaymentEvent(request, PaymentStatus.PAYMENT_FAILED);
            await producer.ProduceAsync("payment-events", evt);
            return evt;
        }
    }

    public async Task RefundPayment(PaymentRequestDTO request)
    {
        var txn = await txnRepo.GetTransaction(request.OrderId);
        if (txn != null && txn.Status == "APPROVED")
        {
            var balance = await balanceRepo.GetBalance(request.UserId);
            if (balance != null)
            {
                balance.Balance += txn.Amount;
                await balanceRepo.UpdateBalance(balance);
                txn.Status = "REFUNDED";
                await txnRepo.UpdateTransaction(txn);
            }
        }
    }

    public async Task<UserBalance?> GetUserBalance(int userId) => await balanceRepo.GetBalance(userId);
}

public interface IKafkaProducer
{
    Task ProduceAsync<T>(string topic, T message);
}
