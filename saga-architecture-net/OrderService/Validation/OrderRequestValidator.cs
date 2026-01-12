using FluentValidation;
using Saga.OrderService.Common;

namespace Saga.OrderService.Validation;

public class OrderRequestValidator : AbstractValidator<OrderRequestDTO>
{
    public OrderRequestValidator()
    {
        RuleFor(x => x.UserId)
            .GreaterThan(0)
            .WithMessage("UserId must be greater than 0");

        RuleFor(x => x.ProductId)
            .GreaterThan(0)
            .WithMessage("ProductId must be greater than 0");

        RuleFor(x => x.Amount)
            .GreaterThanOrEqualTo(0)
            .WithMessage("Amount cannot be negative");
    }
}
