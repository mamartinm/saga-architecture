namespace Saga.PaymentService.Common;

public record PaymentRequestDTO(int UserId, Guid OrderId, double Amount);
