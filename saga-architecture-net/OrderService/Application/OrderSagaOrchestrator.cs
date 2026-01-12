using Saga.OrderService.Common;
using Saga.OrderService.Domain;
using Confluent.Kafka;
using System.Text.Json;

namespace Saga.OrderService.Application;

public class OrderSagaOrchestrator : BackgroundService
{
    private readonly IKafkaProducer _producer;
    private readonly IServiceProvider _serviceProvider;
    private readonly IConfiguration _configuration;
    private readonly ILogger<OrderSagaOrchestrator> _logger;

    public OrderSagaOrchestrator(IKafkaProducer producer, IServiceProvider serviceProvider, IConfiguration configuration, ILogger<OrderSagaOrchestrator> logger)
    {
        _producer = producer;
        _serviceProvider = serviceProvider;
        _configuration = configuration;
        _logger = logger;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        var config = new ConsumerConfig
        {
            BootstrapServers = _configuration["Kafka:BootstrapServers"],
            GroupId = "order-group",
            AutoOffsetReset = AutoOffsetReset.Earliest
        };

        using var consumer = new ConsumerBuilder<string, string>(config).Build();
        consumer.Subscribe(new[] { "order-events", "payment-events", "inventory-events" });

        while (!stoppingToken.IsCancellationRequested)
        {
            try
            {
                var consumeResult = consumer.Consume(stoppingToken);
                var topic = consumeResult.Topic;
                var message = consumeResult.Message.Value;

                if (topic == "order-events")
                {
                    var orderEvent = JsonSerializer.Deserialize<OrderEvent>(message);
                    if (orderEvent?.Status == OrderStatus.ORDER_CREATED)
                    {
                        var paymentRequest = new PaymentRequestDTO(orderEvent.OrderRequest.UserId, orderEvent.OrderRequest.OrderId!.Value, orderEvent.OrderRequest.Amount);
                        await _producer.ProduceAsync("payment-commands", paymentRequest);
                    }
                }
                else if (topic == "payment-events")
                {
                    var paymentEvent = JsonSerializer.Deserialize<PaymentEvent>(message);
                    if (paymentEvent?.Status == PaymentStatus.PAYMENT_COMPLETED)
                    {
                        var inventoryRequest = new InventoryRequestDTO(paymentEvent.PaymentRequest.UserId, 101, paymentEvent.PaymentRequest.OrderId);
                        await _producer.ProduceAsync("inventory-commands", inventoryRequest);
                    }
                    else
                    {
                        await UpdateOrderStatus(paymentEvent!.PaymentRequest.OrderId, OrderStatus.ORDER_CANCELLED);
                    }
                }
                else if (topic == "inventory-events")
                {
                    var inventoryEvent = JsonSerializer.Deserialize<InventoryEvent>(message);
                    if (inventoryEvent?.Status == InventoryStatus.INVENTORY_RESERVED)
                    {
                        await UpdateOrderStatus(inventoryEvent.InventoryRequest.OrderId, OrderStatus.ORDER_COMPLETED);
                    }
                    else
                    {
                        var refundCmd = new PaymentRequestDTO(inventoryEvent!.InventoryRequest.UserId, inventoryEvent.InventoryRequest.OrderId, 0.0);
                        await _producer.ProduceAsync("payment-commands", refundCmd);
                        await UpdateOrderStatus(inventoryEvent.InventoryRequest.OrderId, OrderStatus.ORDER_CANCELLED);
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error processing saga event");
            }
        }
    }

    private async Task UpdateOrderStatus(Guid orderId, OrderStatus status)
    {
        using var scope = _serviceProvider.CreateScope();
        var repository = scope.ServiceProvider.GetRequiredService<IOrderRepository>();
        var order = await repository.GetOrderById(orderId);
        if (order != null)
        {
            order.OrderStatus = status;
            await repository.UpdateOrder(order);
        }
    }
}
