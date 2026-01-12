using FluentValidation;
using Saga.PaymentService.Common;

namespace Saga.PaymentService.Validation;

public class PaymentRequestValidator : AbstractValidator<PaymentRequestDTO>
{
    public PaymentRequestValidator()
    {
        RuleFor(x => x.UserId)
            .GreaterThan(0)
            .WithMessage("UserId must be greater than 0");

        RuleFor(x => x.OrderId)
            .NotEmpty()
            .WithMessage("OrderId is required");

        RuleFor(x => x.Amount)
            .GreaterThanOrEqualTo(0)
            .WithMessage("Amount cannot be negative");
    }
}
