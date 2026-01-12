namespace Saga.OrderService.Common;

public record PaymentRequestDTO(int UserId, Guid OrderId, double Amount);
