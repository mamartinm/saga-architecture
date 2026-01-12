using Microsoft.AspNetCore.Mvc;
using Saga.OrderService.Common;
using Saga.OrderService.Application;
using Saga.OrderService.Domain;
using AutoMapper;

namespace Saga.OrderService.Controllers;

[ApiController]
[Route("orders")]
public class OrderController(OrderAppService orderService, IMapper mapper) : ControllerBase
{
    [HttpPost]
    public async Task<IActionResult> CreateOrder([FromBody] OrderRequestDTO orderRequestDTO)
    {
        var purchaseOrder = mapper.Map<PurchaseOrder>(orderRequestDTO);
        var order = await orderService.CreateOrder(purchaseOrder);
        return Ok(order);
    }
}
