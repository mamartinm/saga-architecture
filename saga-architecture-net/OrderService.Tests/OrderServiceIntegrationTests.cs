using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.AspNetCore.Hosting;
using Saga.OrderService.Common;
using Microsoft.Extensions.DependencyInjection;
using Saga.OrderService.Application;
using Saga.OrderService.Infrastructure;
using Microsoft.EntityFrameworkCore;
using System.Net.Http.Json;
using Xunit;

namespace Saga.OrderService.Tests;

public class OrderServiceIntegrationTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly WebApplicationFactory<Program> _factory;

    public OrderServiceIntegrationTests(WebApplicationFactory<Program> factory)
    {
        _factory = factory.WithWebHostBuilder(builder =>
        {
            builder.UseEnvironment("Testing");
            builder.ConfigureServices(services =>
            {
                // Mock Kafka Producer
                services.AddSingleton<IKafkaProducer, DummyKafkaProducer>();
            });
        });
    }

    private class DummyKafkaProducer : IKafkaProducer
    {
        public Task ProduceAsync<T>(string topic, T message) => Task.CompletedTask;
    }

    [Fact]
    public async Task TestOrderCreation()
    {
        // Given
        var client = _factory.CreateClient();
        var request = new OrderRequestDTO(1, 101, 100.0, null);

        // When
        var response = await client.PostAsJsonAsync("/orders", request);

        // Then
        response.EnsureSuccessStatusCode();

        // Verify order saved in DB
        using var scope = _factory.Services.CreateScope();
        var db = scope.ServiceProvider.GetRequiredService<OrderDbContext>();
        var orderCount = await db.Orders.CountAsync();
        Assert.True(orderCount > 0);
    }

    [Fact]
    public async Task TestOrderCreationWithInvalidAmount_ReturnsBadRequest()
    {
        // Given
        var client = _factory.CreateClient();
        var request = new OrderRequestDTO(1, 101, -50.0, null); // Negative amount

        // When
        var response = await client.PostAsJsonAsync("/orders", request);

        // Then - FluentValidation should reject negative amounts
        Assert.Equal(System.Net.HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task TestOrderCreationWithInvalidUserId_ReturnsBadRequest()
    {
        // Given
        var client = _factory.CreateClient();
        var request = new OrderRequestDTO(0, 101, 100.0, null); // Invalid UserId

        // When
        var response = await client.PostAsJsonAsync("/orders", request);

        // Then - FluentValidation should reject UserId <= 0
        Assert.Equal(System.Net.HttpStatusCode.BadRequest, response.StatusCode);
    }
}
