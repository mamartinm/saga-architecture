using AutoMapper;
using Saga.OrderService.Common;
using Saga.OrderService.Domain;

namespace Saga.OrderService.Application;

public class OrderMapperProfile : Profile
{
    public OrderMapperProfile()
    {
        CreateMap<OrderRequestDTO, PurchaseOrder>()
            .ForMember(dest => dest.Price, opt => opt.MapFrom(src => src.Amount))
            .ForMember(dest => dest.Id, opt => opt.Ignore())
            .ForMember(dest => dest.OrderStatus, opt => opt.Ignore());

        CreateMap<PurchaseOrder, OrderRequestDTO>()
            .ForMember(dest => dest.Amount, opt => opt.MapFrom(src => src.Price))
            .ForMember(dest => dest.OrderId, opt => opt.MapFrom(src => src.Id));
    }
}
