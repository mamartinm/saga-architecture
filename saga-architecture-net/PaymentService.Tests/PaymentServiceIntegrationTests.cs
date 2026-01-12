using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.DependencyInjection;
using Saga.PaymentService.Application;
using Saga.PaymentService.Infrastructure;
using Saga.PaymentService.Domain;
using Microsoft.EntityFrameworkCore;
using Saga.PaymentService.Common;
using System.Net.Http.Json;
using Xunit;

namespace Saga.PaymentService.Tests;

public class PaymentServiceIntegrationTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly WebApplicationFactory<Program> _factory;

    public PaymentServiceIntegrationTests(WebApplicationFactory<Program> factory)
    {
        _factory = factory.WithWebHostBuilder(builder =>
        {
            builder.UseEnvironment("Testing");
            builder.ConfigureServices(services =>
            {
                services.AddSingleton<IKafkaProducer, DummyKafkaProducer>();
            });
        });

        // Seed test data
        SeedTestData();
    }

    private void SeedTestData()
    {
        using var scope = _factory.Services.CreateScope();
        var db = scope.ServiceProvider.GetRequiredService<PaymentDbContext>();
        db.Database.EnsureCreated();

        if (!db.Balances.Any())
        {
            db.Balances.AddRange(
                new UserBalance { UserId = 1, Balance = 1000.0 },
                new UserBalance { UserId = 2, Balance = 50.0 }
            );
            db.SaveChanges();
        }
    }

    private class DummyKafkaProducer : IKafkaProducer
    {
        public Task ProduceAsync<T>(string topic, T message) => Task.CompletedTask;
    }

    [Fact]
    public async Task TestGetBalance()
    {
        // Given
        var client = _factory.CreateClient();
        int userId = 1;

        // When
        var response = await client.GetAsync($"/payments/balance/{userId}");

        // Then
        response.EnsureSuccessStatusCode();
        var balance = await response.Content.ReadFromJsonAsync<dynamic>();
        Assert.NotNull(balance);
    }

    [Fact]
    public async Task TestPaymentProcessing()
    {
        // Given
        var orderId = Guid.NewGuid();
        var request = new PaymentRequestDTO(1, orderId, 100.0);

        // When
        using var scope = _factory.Services.CreateScope();
        var paymentService = scope.ServiceProvider.GetRequiredService<Saga.PaymentService.Application.PaymentService>();
        await paymentService.ProcessPayment(request);

        // Then
        var db = scope.ServiceProvider.GetRequiredService<PaymentDbContext>();
        var userBalance = await db.Balances.FindAsync(1);
        Assert.Equal(900.0, userBalance!.Balance);
    }

    [Fact]
    public async Task TestPaymentRejectedWhenInsufficientBalance()
    {
        // Given - User 2 only has $50
        var orderId = Guid.NewGuid();
        var request = new PaymentRequestDTO(2, orderId, 100.0);

        // When
        using var scope = _factory.Services.CreateScope();
        var paymentService = scope.ServiceProvider.GetRequiredService<Saga.PaymentService.Application.PaymentService>();
        var result = await paymentService.ProcessPayment(request);

        // Then - Payment should be rejected
        Assert.Equal(PaymentStatus.PAYMENT_FAILED, result.Status);
    }
}
