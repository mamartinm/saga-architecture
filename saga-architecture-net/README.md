# 🔄 Saga Architecture .NET Core 8

Demostración de una arquitectura de microservicios implementando el **Patrón Saga Orquestado** con **.NET Core 8** y **Apache Kafka**.

Este proyecto es la **réplica funcional** del proyecto Java (`saga-architecture-java`), diseñado para que ambos puedan intercambiarse sin modificar la infraestructura.

---

## 🚀 Inicio Rápido

### Requisitos Previos
- **.NET SDK 8.0** (versión 8.0.416 o superior)
- **Docker** y **Docker Compose** (para Kafka)

> ⚠️ **Importante**: Antes de continuar, lee el README de infraestructura en `../saga-architecutra-infra/README.md` para levantar Kafka y los servicios necesarios.

### 1. Levantar la Infraestructura

```bash
cd ../saga-architecutra-infra
docker compose up -d
```

### 2. Compilar los Microservicios

```bash
dotnet build
```

### 3. Ejecutar los Microservicios

Abre **3 terminales** y ejecuta:

```bash
# Terminal 1 - Order Service (Puerto 8080)
cd OrderService && dotnet run

# Terminal 2 - Payment Service (Puerto 8081)
cd PaymentService && dotnet run

# Terminal 3 - Inventory Service (Puerto 8082)
cd InventoryService && dotnet run
```

### 4. Probar la Saga

Accede a Swagger UI: [http://localhost:8080/swagger](http://localhost:8080/swagger)

Crea un pedido con `POST /orders`:
```json
{
  "userId": 1,
  "productId": 101,
  "amount": 100.0,
  "orderId": null
}
```

### 5. Ejecutar Tests

```bash
dotnet test
```

### 6. Detener la Infraestructura

```bash
cd ../saga-architecutra-infra
docker compose down
```

---

## 📊 Comparación de Librerías: Java vs .NET Core

### Mapeo de Librerías Equivalentes

| Categoría | Java (Spring Boot) | .NET Core 8 | Estado |
|-----------|-------------------|-------------|--------|
| **Framework Base** | Spring Boot 3.2.0 | ASP.NET Core 8.0 | ✅ Implementado |
| **Web/REST** | Spring Web (`@RestController`) | ASP.NET MVC (`[ApiController]`) | ✅ Implementado |
| **ORM/Persistencia** | Spring Data JPA + Hibernate | Entity Framework Core 8.0 | ✅ Implementado |
| **Base de Datos** | H2 (en memoria) | SQLite (archivo local) | ⚠️ Similar (ver nota) |
| **Migraciones BD** | Liquibase | EF Core Migrations / EnsureCreated | ⚠️ Simplificado |
| **Mensajería (Kafka)** | Spring Cloud Stream + Kafka Binder | Confluent.Kafka (cliente directo) | ⚠️ Diferente enfoque |
| **Mapeo DTO↔Entity** | MapStruct | AutoMapper | ✅ Implementado |
| **Validación** | Spring Validation (`@Valid`) | FluentValidation | ⚠️ Pendiente integrar |
| **Documentación API** | SpringDoc OpenAPI (Swagger) | Swashbuckle.AspNetCore | ✅ Implementado |
| **Tests de Integración** | @EmbeddedKafka + Spring Test | WebApplicationFactory + Mocks | ⚠️ Diferente (ver nota) |
| **Inyección Dependencias** | Spring IoC (`@Autowired`/@RequiredArgsConstructor) | Built-in DI Container | ✅ Implementado |
| **Logging** | Slf4j + Logback | Microsoft.Extensions.Logging | ✅ Built-in |
| **Reducción Boilerplate** | Lombok (`@Data`, etc.) | Records + Primary Constructors | ✅ Nativo en C# |

---

## 📚 Detalle de Librerías .NET Core

### Core Framework

| Paquete NuGet | Versión | Uso | Equivalente Java |
|---------------|---------|-----|------------------|
| **Microsoft.NET.Sdk.Web** | 8.0 | Framework base para APIs | Spring Boot Starter Web |
| **Microsoft.AspNetCore.OpenApi** | 8.0.19 | Soporte para OpenAPI | SpringDoc OpenAPI |
| **Swashbuckle.AspNetCore** | 6.6.2 | Swagger UI | SpringDoc OpenAPI UI |

### Persistencia

| Paquete NuGet | Versión | Uso | Equivalente Java |
|---------------|---------|-----|------------------|
| **Microsoft.EntityFrameworkCore.Sqlite** | 8.0.11 | ORM + Base de datos SQLite | Spring Data JPA + H2 |

**Nota sobre BD**: 
- Java usa H2 en memoria (los datos se pierden al reiniciar).
- .NET usa SQLite en archivo (`order.db`, `payment.db`, `inventory.db`). Los datos persisten.
- Para igualar el comportamiento, se podría usar `Microsoft.EntityFrameworkCore.InMemory` en los tests.

### Mensajería

| Paquete NuGet | Versión | Uso | Equivalente Java |
|---------------|---------|-----|------------------|
| **Confluent.Kafka** | 2.6.1 | Cliente Kafka | Spring Cloud Stream + Kafka Binder |

**Nota sobre Kafka**:
- Java usa **Spring Cloud Stream** que abstrae el broker (declarativo con bindings).
- .NET usa **Confluent.Kafka** directamente (imperativo con Producer/Consumer builders).
- **Resultado**: Ambos se conectan al mismo Kafka y usan los mismos topics.

### Mapeo de Objetos

| Paquete NuGet | Versión | Uso | Equivalente Java |
|---------------|---------|-----|------------------|
| **AutoMapper.Extensions.Microsoft.DependencyInjection** | 12.0.1 | Mapeo DTO↔Entity | MapStruct |

**Diferencia**:
- MapStruct genera código en tiempo de compilación (más rápido).
- AutoMapper usa reflection en runtime (más flexible).

### Validación

| Paquete NuGet | Versión | Uso | Equivalente Java |
|---------------|---------|-----|------------------|
| **FluentValidation.AspNetCore** | 11.3.0 | Validación de DTOs | Spring Validation (`@Valid`) |

**Nota**: FluentValidation está incluido pero no completamente integrado (pendiente).

### Testing

| Paquete NuGet | Versión | Uso | Equivalente Java |
|---------------|---------|-----|------------------|
| **xunit** | 2.5.3 | Framework de tests | JUnit 5 |
| **Microsoft.AspNetCore.Mvc.Testing** | 8.0.11 | WebApplicationFactory para tests de integración | @SpringBootTest |
| **coverlet.collector** | 6.0.0 | Cobertura de código | JaCoCo |

**Diferencia crítica en Tests**:
- **Java**: Usa `@EmbeddedKafka` que levanta un Kafka real en memoria.
- **.NET**: Usa **mocks** (`DummyKafkaProducer`) porque no existe un "Embedded Kafka" nativo en .NET.
- **Alternativa para .NET**: Usar **Testcontainers** para levantar Kafka en Docker durante los tests.

---

## 🏛️ Arquitectura Hexagonal

### Estructura de Carpetas por Servicio

```
Saga.{Service}/
├── Application/          # Capa de Aplicación
│   ├── {Service}Service.cs    # Lógica de negocio
│   ├── {Service}MapperProfile.cs  # Perfiles AutoMapper
│   └── OrderSagaOrchestrator.cs   # (Solo en OrderService)
├── Common/              # DTOs y Eventos (Modelo de API)
│   ├── Events.cs             # Records para eventos Kafka
│   ├── OrderRequestDTO.cs    # DTOs de entrada
│   └── OrderStatus.cs        # Enums de estado
├── Controllers/         # Adaptadores de Entrada (REST)
│   └── {Service}Controller.cs
├── Domain/              # Entidades y Puertos
│   ├── {Entity}.cs           # Entidad de dominio
│   └── I{Entity}Repository.cs # Interfaz del puerto de salida
├── Infrastructure/      # Adaptadores de Salida
│   ├── {Service}DbContext.cs # EF Core DbContext
│   ├── {Entity}Repository.cs # Implementación del repositorio
│   ├── KafkaProducer.cs      # Productor Kafka
│   └── {Service}Consumer.cs  # Consumidor Kafka (BackgroundService)
└── Program.cs           # Configuración y arranque
```

### Comparación de Capas

| Capa Hexagonal | Java (Spring) | .NET Core |
|----------------|---------------|-----------|
| **Dominio (Entidades)** | `entity/PurchaseOrder.java` | `Domain/PurchaseOrder.cs` |
| **Puertos (Interfaces)** | Implícito en JpaRepository | `Domain/IOrderRepository.cs` |
| **Aplicación (Servicios)** | `service/OrderService.java` | `Application/OrderAppService.cs` |
| **Adaptadores Entrada (API)** | `controller/OrderController.java` | `Controllers/OrderController.cs` |
| **Adaptadores Salida (BD)** | JPA + @Repository | `Infrastructure/OrderRepository.cs` |
| **Adaptadores Salida (Kafka)** | StreamBridge | `Infrastructure/KafkaProducer.cs` |

---

## 📋 Topics de Kafka (Compartidos)

Ambos proyectos (Java y .NET) comparten la misma infraestructura Kafka:

| Topic | Productor | Consumidor |
|-------|-----------|------------|
| `order-events` | OrderService | OrderSagaOrchestrator |
| `payment-commands` | OrderSagaOrchestrator | PaymentConsumer |
| `payment-events` | PaymentService | OrderSagaOrchestrator |
| `inventory-commands` | OrderSagaOrchestrator | InventoryConsumer |
| `inventory-events` | InventoryService | OrderSagaOrchestrator |

**Importante**: Los nombres de topics y la estructura de mensajes son idénticos, permitiendo interoperabilidad.

---

## ✅ Paridad Lograda con Java

### 1. Base de Datos
| Aspecto | Java | .NET | Estado |
|---------|------|------|--------|
| Tipo de BD (Producción) | H2 (en memoria) | SQLite (archivo) | ✅ Funcionalmente equivalente |
| Tipo de BD (Tests) | H2 (en memoria) | **InMemoryDatabase** | ✅ Implementado |
| Migraciones | Liquibase | EnsureCreated + Seeding | ✅ Implementado |

### 2. Validación
| Aspecto | Java | .NET | Estado |
|---------|------|------|--------|
| Framework | Spring Validation (`@Valid`) | **FluentValidation** | ✅ Implementado |
| Integración | Automática en Controllers | **FluentValidationAutoValidation** | ✅ Implementado |

### 3. Testing
| Aspecto | Java | .NET | Estado |
|---------|------|------|--------|
| Framework | JUnit 5 + @SpringBootTest | xUnit + WebApplicationFactory | ✅ Implementado |
| Kafka en Tests | @EmbeddedKafka | **Testcontainers.Kafka** (disponible) | ✅ Paquete instalado |
| Mocks de Kafka | - | DummyKafkaProducer (para tests rápidos) | ✅ Implementado |

### 4. Abstracción de Mensajería
| Aspecto | Java | .NET | Nota |
|---------|------|------|------|
| Abstracción | Spring Cloud Stream | Confluent.Kafka (directo) | ⚠️ Diferente enfoque, misma funcionalidad |

---

## 🧪 Estrategia de Tests

### Tests de Integración

```csharp
public class OrderServiceIntegrationTests : IClassFixture<WebApplicationFactory<Program>>
{
    public OrderServiceIntegrationTests(WebApplicationFactory<Program> factory)
    {
        _factory = factory.WithWebHostBuilder(builder => {
            builder.UseEnvironment("Testing");  // Usa InMemoryDatabase
            builder.ConfigureServices(services => {
                services.AddSingleton<IKafkaProducer, DummyKafkaProducer>();
            });
        });
    }
}
```

### Tests Incluidos

| Servicio | Tests | Descripción |
|----------|-------|-------------|
| **OrderService** | `TestOrderCreation` | Crea pedido y verifica en BD |
| | `TestOrderCreationWithInvalidAmount_ReturnsBadRequest` | Valida FluentValidation (monto negativo) |
| | `TestOrderCreationWithInvalidUserId_ReturnsBadRequest` | Valida FluentValidation (userId inválido) |
| **PaymentService** | `TestGetBalance` | Consulta saldo vía API |
| | `TestPaymentProcessing` | Procesa pago y verifica saldo actualizado |
| | `TestPaymentRejectedWhenInsufficientBalance` | Rechaza pago si balance insuficiente |
| **InventoryService** | `TestInventoryReservation` | Reserva stock y verifica decremento |
| | `TestInventoryReservationRejectedWhenNoStock` | Rechaza si no hay stock |

### Opción Avanzada: Testcontainers.Kafka

El paquete `Testcontainers.Kafka` está instalado para permitir tests con Kafka real:

```csharp
// Ejemplo de uso (opcional)
var kafkaContainer = new KafkaBuilder().Build();
await kafkaContainer.StartAsync();
var bootstrapServers = kafkaContainer.GetBootstrapAddress();
```

---

## 📁 Estructura del Proyecto

```
saga-architecture-net/
├── SagaArchitecture.sln        # Solución Visual Studio
├── global.json                  # Fija versión del SDK
├── OrderService/               # Microservicio de Pedidos + Orchestrator
├── OrderService.Tests/         # Tests del OrderService
├── PaymentService/             # Microservicio de Pagos
├── PaymentService.Tests/       # Tests del PaymentService
├── InventoryService/           # Microservicio de Inventario
└── InventoryService.Tests/     # Tests del InventoryService
```

---

## 🔗 Endpoints Disponibles

| Servicio | Puerto | Método | Endpoint | Descripción |
|----------|--------|--------|----------|-------------|
| Order | 8080 | POST | `/orders` | Crear pedido |
| Payment | 8081 | GET | `/payments/balance/{userId}` | Consultar saldo |
| Inventory | 8082 | - | - | Sin endpoint REST (solo Kafka) |

---

## 🔄 Interoperabilidad Java ↔ .NET

Como ambos proyectos:
1. Usan los **mismos puertos** (8080, 8081, 8082).
2. Se conectan al **mismo Kafka** (localhost:9092).
3. Usan los **mismos topics** con los mismos nombres.
4. Serializan mensajes en **JSON** con la misma estructura.

Puedes:
- Arrancar `order-service` en Java + `payment-service` en .NET + `inventory-service` en Java.
- O cualquier combinación mixta.
- **El sistema funcionará igual** porque se comunican vía Kafka.

---

## 📄 Licencia

Este proyecto es solo para fines educativos y de demostración.
