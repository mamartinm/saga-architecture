using FluentValidation;
using FluentValidation.AspNetCore;
using Microsoft.EntityFrameworkCore;
using Saga.OrderService.Application;
using Saga.OrderService.Domain;
using Saga.OrderService.Infrastructure;
using Saga.OrderService.Validation;

namespace Saga.OrderService;

public class Program
{
    public static void Main(string[] args)
    {
        var builder = WebApplication.CreateBuilder(args);

        // Add CORS
        builder.Services.AddCors(options => {
            options.AddDefaultPolicy(policy => {
                policy.WithOrigins("http://localhost:4200")
                      .AllowAnyHeader()
                      .AllowAnyMethod();
            });
        });

        // Add services to the container.
        builder.Services.AddControllers();
        builder.Services.AddEndpointsApiExplorer();
        builder.Services.AddSwaggerGen();

        // FluentValidation
        builder.Services.AddFluentValidationAutoValidation();
        builder.Services.AddValidatorsFromAssemblyContaining<OrderRequestValidator>();

        // Database: Use InMemory for Testing, SQLite for Development/Production
        if (builder.Environment.EnvironmentName == "Testing")
        {
            builder.Services.AddDbContext<OrderDbContext>(options =>
                options.UseInMemoryDatabase("OrderTestDb"));
        }
        else
        {
            builder.Services.AddDbContext<OrderDbContext>(options =>
                options.UseSqlite(builder.Configuration.GetConnectionString("DefaultConnection")));
        }

        builder.Services.AddScoped<IOrderRepository, OrderRepository>();
        builder.Services.AddScoped<OrderAppService>();
        builder.Services.AddSingleton<IKafkaProducer, KafkaProducer>();
        builder.Services.AddAutoMapper(typeof(OrderMapperProfile));
        
        if (builder.Environment.EnvironmentName != "Testing")
        {
            builder.Services.AddHostedService<OrderSagaOrchestrator>();
        }

        var app = builder.Build();

        // Configure the HTTP request pipeline.
        if (app.Environment.IsDevelopment())
        {
            app.UseSwagger();
            app.UseSwaggerUI();
        }

        app.UseCors();
        app.UseAuthorization();
        app.MapControllers();

        // Ensure DB created (for SQLite mode)
        if (builder.Environment.EnvironmentName != "Testing")
        {
            using var scope = app.Services.CreateScope();
            var db = scope.ServiceProvider.GetRequiredService<OrderDbContext>();
            db.Database.EnsureCreated();
        }

        app.Run();
    }
}

