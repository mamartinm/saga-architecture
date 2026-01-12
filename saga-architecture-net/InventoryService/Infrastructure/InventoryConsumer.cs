using Saga.InventoryService.Common;
using Saga.InventoryService.Application;
using Confluent.Kafka;
using System.Text.Json;

namespace Saga.InventoryService.Infrastructure;

public class InventoryConsumer : BackgroundService
{
    private readonly IServiceProvider _serviceProvider;
    private readonly IConfiguration _configuration;
    private readonly ILogger<InventoryConsumer> _logger;

    public InventoryConsumer(IServiceProvider serviceProvider, IConfiguration configuration, ILogger<InventoryConsumer> logger)
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
            GroupId = "inventory-group",
            AutoOffsetReset = AutoOffsetReset.Earliest
        };

        using var consumer = new ConsumerBuilder<string, string>(config).Build();
        consumer.Subscribe("inventory-commands");

        while (!stoppingToken.IsCancellationRequested)
        {
            try
            {
                var consumeResult = consumer.Consume(stoppingToken);
                var request = JsonSerializer.Deserialize<InventoryRequestDTO>(consumeResult.Message.Value);
                
                if (request != null)
                {
                    using var scope = _serviceProvider.CreateScope();
                    var inventoryService = scope.ServiceProvider.GetRequiredService<Application.InventoryService>();
                    await inventoryService.DeductInventory(request);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error processing inventory command");
            }
        }
    }
}
