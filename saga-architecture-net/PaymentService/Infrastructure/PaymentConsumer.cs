using Saga.PaymentService.Common;
using Saga.PaymentService.Application;
using Confluent.Kafka;
using System.Text.Json;

namespace Saga.PaymentService.Infrastructure;

public class PaymentConsumer : BackgroundService
{
    private readonly IServiceProvider _serviceProvider;
    private readonly IConfiguration _configuration;
    private readonly ILogger<PaymentConsumer> _logger;

    public PaymentConsumer(IServiceProvider serviceProvider, IConfiguration configuration, ILogger<PaymentConsumer> logger)
    {
        _serviceProvider = serviceProvider;
        _configuration = configuration;
        _logger = logger;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        var config = new ConsumerConfig
        {
            BootstrapServers = _configuration["Kafka:BootstrapServers"],
            GroupId = "payment-group",
            AutoOffsetReset = AutoOffsetReset.Earliest
        };

        using var consumer = new ConsumerBuilder<string, string>(config).Build();
        consumer.Subscribe("payment-commands");

        while (!stoppingToken.IsCancellationRequested)
        {
            try
            {
                var consumeResult = consumer.Consume(stoppingToken);
                var request = JsonSerializer.Deserialize<PaymentRequestDTO>(consumeResult.Message.Value);
                
                if (request != null)
                {
                    using var scope = _serviceProvider.CreateScope();
                    var paymentService = scope.ServiceProvider.GetRequiredService<Application.PaymentService>();
                    
                    if (request.Amount > 0)
                    {
                        await paymentService.ProcessPayment(request);
                    }
                    else
                    {
                        await paymentService.RefundPayment(request);
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error processing payment command");
            }
        }
    }
}
