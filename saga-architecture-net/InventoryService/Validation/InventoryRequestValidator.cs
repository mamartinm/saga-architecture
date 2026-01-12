using FluentValidation;
using Saga.InventoryService.Common;

namespace Saga.InventoryService.Validation;

public class InventoryRequestValidator : AbstractValidator<InventoryRequestDTO>
{
    public InventoryRequestValidator()
    {
        RuleFor(x => x.UserId)
            .GreaterThan(0)
            .WithMessage("UserId must be greater than 0");

        RuleFor(x => x.ProductId)
            .GreaterThan(0)
            .WithMessage("ProductId must be greater than 0");

        RuleFor(x => x.OrderId)
            .NotEmpty()
            .WithMessage("OrderId is required");
    }
}
