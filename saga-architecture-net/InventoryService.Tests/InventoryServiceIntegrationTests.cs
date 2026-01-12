using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.DependencyInjection;
using Saga.InventoryService.Application;
using Saga.InventoryService.Infrastructure;
using Saga.InventoryService.Domain;
using Microsoft.EntityFrameworkCore;
using Saga.InventoryService.Common;
using Xunit;

namespace Saga.InventoryService.Tests;

public class InventoryServiceIntegrationTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly WebApplicationFactory<Program> _factory;

    public InventoryServiceIntegrationTests(WebApplicationFactory<Program> factory)
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
        var db = scope.ServiceProvider.GetRequiredService<InventoryDbContext>();
        db.Database.EnsureCreated();

        if (!db.Products.Any())
        {
            db.Products.AddRange(
                new Product { ProductId = 101, AvailableStock = 10 },
                new Product { ProductId = 102, AvailableStock = 0 }
            );
            db.SaveChanges();
        }
    }

    private class DummyKafkaProducer : IKafkaProducer
    {
        public Task ProduceAsync<T>(string topic, T message) => Task.CompletedTask;
    }

    [Fact]
    public async Task TestInventoryReservation()
    {
        // Given
        var orderId = Guid.NewGuid();
        var request = new InventoryRequestDTO(1, 101, orderId);

        // When
        using var scope = _factory.Services.CreateScope();
        var inventoryService = scope.ServiceProvider.GetRequiredService<Saga.InventoryService.Application.InventoryService>();
        var result = await inventoryService.DeductInventory(request);

        // Then
        Assert.Equal(InventoryStatus.INVENTORY_RESERVED, result.Status);
        var db = scope.ServiceProvider.GetRequiredService<InventoryDbContext>();
        var product = await db.Products.FindAsync(101);
        Assert.Equal(9, product!.AvailableStock);
    }

    [Fact]
    public async Task TestInventoryReservationRejectedWhenNoStock()
    {
        // Given - Product 102 has 0 stock
        var orderId = Guid.NewGuid();
        var request = new InventoryRequestDTO(1, 102, orderId);

        // When
        using var scope = _factory.Services.CreateScope();
        var inventoryService = scope.ServiceProvider.GetRequiredService<Saga.InventoryService.Application.InventoryService>();
        var result = await inventoryService.DeductInventory(request);

        // Then - Should be rejected
        Assert.Equal(InventoryStatus.INVENTORY_REJECTED, result.Status);
    }
}
