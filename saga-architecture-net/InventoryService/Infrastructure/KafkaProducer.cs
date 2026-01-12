using Confluent.Kafka;
using Saga.InventoryService.Application;
using System.Text.Json;

namespace Saga.InventoryService.Infrastructure;

public class KafkaProducer : IKafkaProducer
{
    private readonly IProducer<string, string> _producer;

    public KafkaProducer(IConfiguration configuration)
    {
        var config = new ProducerConfig { BootstrapServers = configuration["Kafka:BootstrapServers"] };
        _producer = new ProducerBuilder<string, string>(config).Build();
    }

    public async Task ProduceAsync<T>(string topic, T message)
    {
        var serializedMessage = JsonSerializer.Serialize(message);
        await _producer.ProduceAsync(topic, new Message<string, string> { Value = serializedMessage });
    }
}
