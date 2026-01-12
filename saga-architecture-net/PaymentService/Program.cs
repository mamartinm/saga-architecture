using FluentValidation;
using FluentValidation.AspNetCore;
using Microsoft.EntityFrameworkCore;
using Saga.PaymentService.Application;
using Saga.PaymentService.Infrastructure;
using Saga.PaymentService.Domain;
using Saga.PaymentService.Validation;

namespace Saga.PaymentService;

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

        builder.Services.AddControllers();
        builder.Services.AddEndpointsApiExplorer();
        builder.Services.AddSwaggerGen();

        // FluentValidation
        builder.Services.AddFluentValidationAutoValidation();
        builder.Services.AddValidatorsFromAssemblyContaining<PaymentRequestValidator>();

        // Database: Use InMemory for Testing, SQLite for Development/Production
        if (builder.Environment.EnvironmentName == "Testing")
        {
            builder.Services.AddDbContext<PaymentDbContext>(options =>
                options.UseInMemoryDatabase("PaymentTestDb"));
        }
        else
        {
            builder.Services.AddDbContext<PaymentDbContext>(options =>
                options.UseSqlite(builder.Configuration.GetConnectionString("DefaultConnection")));
        }

        builder.Services.AddScoped<IBalanceRepository, BalanceRepository>();
        builder.Services.AddScoped<IPaymentTransactionRepository, PaymentTransactionRepository>();
        builder.Services.AddScoped<Saga.PaymentService.Application.PaymentService>();
        builder.Services.AddSingleton<IKafkaProducer, KafkaProducer>();
        builder.Services.AddAutoMapper(typeof(PaymentMapperProfile));
        
        if (builder.Environment.EnvironmentName != "Testing")
        {
            builder.Services.AddHostedService<PaymentConsumer>();
        }

        var app = builder.Build();

        if (app.Environment.IsDevelopment())
        {
            app.UseSwagger();
            app.UseSwaggerUI();
        }

        app.UseCors();
        app.UseAuthorization();
        app.MapControllers();

        // Seed data and ensure DB created (for non-testing environments)
        if (builder.Environment.EnvironmentName != "Testing")
        {
            using var scope = app.Services.CreateScope();
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

        app.Run();
    }
}

