namespace Saga.PaymentService.Domain;

public class UserBalance
{
    public int UserId { get; set; }
    public double Balance { get; set; }
}

public class PaymentTransaction
{
    public Guid OrderId { get; set; }
    public int UserId { get; set; }
    public double Amount { get; set; }
    public string Status { get; set; } = "PENDING";
}
