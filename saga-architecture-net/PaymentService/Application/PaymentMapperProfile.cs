using AutoMapper;
using Saga.PaymentService.Domain;
using Saga.PaymentService.Controllers;

namespace Saga.PaymentService.Application;

public class PaymentMapperProfile : Profile
{
    public PaymentMapperProfile()
    {
        CreateMap<UserBalance, UserBalanceResponseDTO>();
    }
}
