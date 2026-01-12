using Microsoft.AspNetCore.Mvc;
using Saga.PaymentService.Application;
using AutoMapper;

namespace Saga.PaymentService.Controllers;

public record UserBalanceResponseDTO(int UserId, double Balance);

[ApiController]
[Route("payments")]
public class PaymentController(Application.PaymentService paymentService, IMapper mapper) : ControllerBase
{
    [HttpGet("balance/{userId}")]
    public async Task<IActionResult> GetBalance(int userId)
    {
        var balance = await paymentService.GetUserBalance(userId);
        if (balance == null) return NotFound();
        return Ok(mapper.Map<UserBalanceResponseDTO>(balance));
    }
}
