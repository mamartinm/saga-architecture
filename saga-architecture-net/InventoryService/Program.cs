using FluentValidation;
using FluentValidation.AspNetCore;
using Microsoft.EntityFrameworkCore;
using Saga.InventoryService.Application;
using Saga.InventoryService.Infrastructure;
using Saga.InventoryService.Domain;
using Saga.InventoryService.Validation;

namespace Saga.InventoryService;

public class Program
{
    public static void Main(string[] args)
    {
        var builder = WebApplication.CreateBuilder(args);

        builder.Services.AddControllers();
        builder.Services.AddEndpointsApiExplorer();
        builder.Services.AddSwaggerGen();

        // FluentValidation
        builder.Services.AddFluentValidationAutoValidation();
        builder.Services.AddValidatorsFromAssemblyContaining<InventoryRequestValidator>();

        // Database: Use InMemory for Testing, SQLite for Development/Production
        if (builder.Environment.EnvironmentName == "Testing")
        {
            builder.Services.AddDbContext<InventoryDbContext>(options =>
                options.UseInMemoryDatabase("InventoryTestDb"));
        }
        else
        {
            builder.Services.AddDbContext<InventoryDbContext>(options =>
                options.UseSqlite(builder.Configuration.GetConnectionString("DefaultConnection")));
        }

        builder.Services.AddScoped<IProductRepository, ProductRepository>();
        builder.Services.AddScoped<Saga.InventoryService.Application.InventoryService>();
        builder.Services.AddSingleton<IKafkaProducer, KafkaProducer>();
        
        if (builder.Environment.EnvironmentName != "Testing")
        {
            builder.Services.AddHostedService<InventoryConsumer>();
        }

        var app = builder.Build();

        if (app.Environment.IsDevelopment())
        {
            app.UseSwagger();
            app.UseSwaggerUI();
        }

        app.UseAuthorization();
        app.MapControllers();

        // Seed data and ensure DB created (for non-testing environments)
        if (builder.Environment.EnvironmentName != "Testing")
        {
            using var scope = app.Services.CreateScope();
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

        app.Run();
    }
}

