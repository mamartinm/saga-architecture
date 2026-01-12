# 🔄 Saga Architecture Demo

Demostración de una arquitectura hexagonal de microservicios implementando el **Patrón Saga Orquestado**, con **Spring Boot 3** y **Apache Kafka**, aplicando los principios de **Domain-Driven Design (DDD)**.

> 🎓 **Proyecto didáctico** - Este proyecto tiene fines educativos y de demostración de patrones arquitectónicos.

---

## 🚀 Inicio Rápido

### Requisitos Previos
- **Java 21** (o superior)
- **Maven 3.9+**
- **Docker** y **Docker Compose**

> ⚠️ **Importante**: Antes de continuar, lee el README de infraestructura en `../saga-architecutra-infra/README.md` para levantar Kafka y los servicios necesarios.

### 1. Levantar la Infraestructura

```bash
cd ../saga-architecutra-infra
docker compose up -d
```

### 2. Compilar los Microservicios

```bash
cd order-service && mvn clean compile && cd ..
cd payment-service && mvn clean compile && cd ..
cd inventory-service && mvn clean compile && cd ..
```

### 3. Ejecutar los Microservicios

Abre **3 terminales** y ejecuta:

```bash
# Terminal 1 - Order Service (Puerto 8080)
cd order-service && mvn spring-boot:run

# Terminal 2 - Payment Service (Puerto 8081)
cd payment-service && mvn spring-boot:run

# Terminal 3 - Inventory Service (Puerto 8082)
cd inventory-service && mvn spring-boot:run
```

### 4. Probar la App

Accede a Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Crea un pedido con `POST /orders`:
```json
{
  "userId": 1,
  "productId": 101,
  "amount": 100.0
}
```

### 5. Ejecutar Tests

```bash
cd order-service && mvn test
cd payment-service && mvn test
cd inventory-service && mvn test
```

---

## 📖 Descripción del Proyecto

Este proyecto implementa un flujo de **e-commerce simplificado** donde crear un pedido dispara una secuencia coordinada de operaciones distribuidas:

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Order Service  │────▶│ Payment Service │────▶│Inventory Service│
│   (Puerto 8080) │     │   (Puerto 8081) │     │   (Puerto 8082) │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
    ┌─────────┐             ┌─────────┐             ┌─────────┐
    │   H2    │             │   H2    │             │   H2    │
    │ orderdb │             │paymentdb│             │inventorydb│
    └─────────┘             └─────────┘             └─────────┘
```

### Flujo de la Saga (Happy Path)

```
1. Cliente POST /orders
         │
         ▼
2. OrderService: Crea orden (ORDER_CREATED)
         │
         ▼ [order-events topic]
         │
3. OrderSagaOrchestrator: Envía comando de pago
         │
         ▼ [payment-commands topic]
         │
4. PaymentService: Procesa pago (PAYMENT_COMPLETED)
         │
         ▼ [payment-events topic]
         │
5. OrderSagaOrchestrator: Envía comando de inventario
         │
         ▼ [inventory-commands topic]
         │
6. InventoryService: Reserva stock (INVENTORY_RESERVED)
         │
         ▼ [inventory-events topic]
         │
7. OrderSagaOrchestrator: Marca orden como COMPLETED
```

### Flujo de Compensación (Fallo)

Si el inventario falla (sin stock), la saga ejecuta compensación:

```
InventoryService: INVENTORY_REJECTED
         │
         ▼
OrderSagaOrchestrator: Envía comando de refund
         │
         ▼
PaymentService: Devuelve el dinero
         │
         ▼
OrderSagaOrchestrator: Marca orden como CANCELLED
```

---

## 🏛️ Arquitectura Hexagonal y DDD

El proyecto sigue los principios de **Arquitectura Hexagonal** (Ports & Adapters) y **Domain-Driven Design (DDD)**.

### Estructura de Paquetes (Order Service)

```
com.example.saga.order/
│
├── 🧠 domain/                          # NÚCLEO DEL DOMINIO
│   ├── model/                          # Entidades y Value Objects
│   │   ├── Order.java                  # Aggregate Root
│   │   ├── OrderId.java                # Value Object
│   │   ├── UserId.java                 # Value Object
│   │   ├── ProductId.java              # Value Object
│   │   ├── Money.java                  # Value Object
│   │   └── OrderStatus.java            # Enum de dominio
│   │
│   ├── event/                          # Eventos de Dominio
│   │   ├── DomainEvent.java            # Interface base
│   │   ├── OrderCreatedDomainEvent.java
│   │   └── OrderStatusChangedDomainEvent.java
│   │
│   ├── exception/                      # Excepciones de Dominio
│   │   ├── DomainException.java
│   │   ├── InvalidOrderStateException.java
│   │   └── OrderNotFoundException.java
│   │
│   └── port/                           # PUERTOS (Interfaces)
│       ├── input/                      # Casos de Uso
│       │   ├── CreateOrderUseCase.java
│       │   ├── GetOrderUseCase.java
│       │   ├── CompleteOrderUseCase.java
│       │   └── CancelOrderUseCase.java
│       │
│       └── output/                     # Dependencias Externas
│           ├── OrderRepository.java
│           ├── DomainEventPublisher.java
│           ├── PaymentCommandSender.java
│           └── InventoryCommandSender.java
│
├── 🔄 application/                     # SERVICIOS DE APLICACIÓN
│   ├── service/
│   │   ├── CreateOrderApplicationService.java
│   │   ├── GetOrderApplicationService.java
│   │   ├── CompleteOrderApplicationService.java
│   │   └── CancelOrderApplicationService.java
│   │
│   └── saga/
│       └── OrderSagaOrchestrator.java   # Orquestador de Saga
│
└── 🔌 infrastructure/                  # ADAPTADORES
    └── adapter/
        ├── input/                       # Adaptadores de Entrada
        │   ├── rest/
        │   │   ├── OrderRestController.java
        │   │   ├── CreateOrderRequest.java
        │   │   ├── OrderResponse.java
        │   │   └── GlobalExceptionHandler.java
        │   │
        │   └── messaging/
        │       └── SagaEventConsumers.java
        │
        └── output/                      # Adaptadores de Salida
            ├── persistence/
            │   ├── OrderJpaEntity.java
            │   ├── OrderJpaRepository.java
            │   ├── OrderPersistenceMapper.java
            │   └── OrderRepositoryAdapter.java
            │
            └── messaging/
                ├── KafkaDomainEventPublisher.java
                ├── KafkaPaymentCommandSender.java
                └── KafkaInventoryCommandSender.java
```

### Principios Implementados

#### Domain-Driven Design (DDD)

| Concepto | Implementación |
|----------|----------------|
| **Aggregate Root** | `Order.java` encapsula lógica de transiciones de estado |
| **Value Objects** | `OrderId`, `UserId`, `ProductId`, `Money` son inmutables con validación |
| **Domain Events** | `OrderCreatedDomainEvent`, `OrderStatusChangedDomainEvent` |
| **Bounded Context** | Cada microservicio es un contexto acotado independiente |
| **Ubiquitous Language** | Nombres de clases y métodos reflejan el lenguaje del dominio |
| **Rich Domain Model** | La lógica de negocio vive en las entidades, no en servicios anémicos |

#### Arquitectura Hexagonal

| Concepto | Implementación |
|----------|----------------|
| **Núcleo independiente** | El dominio no tiene dependencias de Spring, JPA, Kafka |
| **Puertos de Entrada** | `CreateOrderUseCase`, `GetOrderUseCase` definen casos de uso |
| **Puertos de Salida** | `OrderRepository`, `PaymentCommandSender` definen dependencias |
| **Adaptadores de Entrada** | `OrderRestController`, `SagaEventConsumers` |
| **Adaptadores de Salida** | `OrderRepositoryAdapter`, `KafkaPaymentCommandSender` |
| **Inversión de dependencias** | La infraestructura implementa interfaces del dominio |

### Flujo de Datos

```
HTTP Request
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│ INFRASTRUCTURE (Adaptador de Entrada)                          │
│   OrderRestController.createOrder(CreateOrderRequest)          │
└─────────────────────────────────────────────────────────────────┘
     │ Convierte a Command
     ▼
┌─────────────────────────────────────────────────────────────────┐
│ APPLICATION (Servicio de Aplicación)                           │
│   CreateOrderApplicationService.execute(CreateOrderCommand)    │
└─────────────────────────────────────────────────────────────────┘
     │ Crea entidad
     ▼
┌─────────────────────────────────────────────────────────────────┐
│ DOMAIN (Entidad de Dominio)                                    │
│   Order.create(userId, productId, price)                       │
│   → Genera OrderCreatedDomainEvent                             │
└─────────────────────────────────────────────────────────────────┘
     │ Persiste vía Puerto
     ▼
┌─────────────────────────────────────────────────────────────────┐
│ INFRASTRUCTURE (Adaptador de Salida)                           │
│   OrderRepositoryAdapter.save(order)                           │
│   KafkaDomainEventPublisher.publish(event)                     │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📚 Stack Tecnológico

### Core Framework

| Librería | Uso |
|----------|-----|
| **Spring Boot 3.2** | Framework base con configuración automática |
| **Spring Web** | Endpoints REST con `@RestController` |
| **Spring Data JPA** | Abstracción sobre JPA/Hibernate |
| **Spring Cloud Stream** | Abstracción de mensajería (desacopla de Kafka) |

### Mensajería

| Librería | Uso |
|----------|-----|
| **Spring Cloud Stream Binder Kafka** | Conexión transparente a Kafka |
| **Spring Kafka** | Cliente Kafka nativo |
| **Spring Kafka Test** | Tests con Kafka embebido |

### Base de Datos

| Librería | Uso |
|----------|-----|
| **H2 Database** | BD en memoria para desarrollo |
| **Liquibase** | Migraciones de esquema versionadas |

### Utilidades

| Librería | Uso |
|----------|-----|
| **Lombok** | Reducción de boilerplate (`@Slf4j`, etc.) |
| **SpringDoc OpenAPI** | Swagger UI automático |
| **Spring Validation** | Validación de DTOs con anotaciones |

---

## 🔧 Configuración

### Topics de Kafka

| Topic | Productores | Consumidores |
|-------|-------------|--------------|
| `order-events` | OrderService | OrderSagaOrchestrator |
| `payment-commands` | OrderSagaOrchestrator | PaymentConsumer |
| `payment-events` | PaymentConsumer | OrderSagaOrchestrator |
| `inventory-commands` | OrderSagaOrchestrator | InventoryConsumer |
| `inventory-events` | InventoryConsumer | OrderSagaOrchestrator |

### Puertos de Servicios

| Servicio | Puerto | Swagger UI |
|----------|--------|------------|
| Order Service | 8080 | http://localhost:8080/swagger-ui.html |
| Payment Service | 8081 | http://localhost:8081/swagger-ui.html |
| Inventory Service | 8082 | http://localhost:8082/swagger-ui.html |

---

## 📁 Estructura del Proyecto

```
saga-architecture-java/
├── README.md                   # Este archivo
│
├── order-service/              # Microservicio de Pedidos + Orquestador Saga
│   ├── src/main/java/
│   │   └── com.example.saga/
│   │       ├── common/         # DTOs para comunicación inter-servicios
│   │       └── order/
│   │           ├── domain/     # Núcleo DDD (model, port, event, exception)
│   │           ├── application/# Servicios de aplicación y Saga
│   │           └── infrastructure/ # Adaptadores (REST, JPA, Kafka)
│   └── src/main/resources/
│       ├── application.yml
│       └── db/changelog/       # Migraciones Liquibase
│
├── payment-service/            # Microservicio de Pagos
│   └── ...                     
│
└── inventory-service/          # Microservicio de Inventario
    └── ...                     
```

---

## 🧪 Testing

### Tests de Dominio (Sin Spring)

El dominio puede testearse sin infraestructura:

```java
@Test
void shouldCreateOrderWithInitialStatus() {
    Order order = Order.create(
        UserId.of(1),
        ProductId.of(101),
        Money.of(100.0)
    );
    
    assertEquals(OrderStatus.CREATED, order.getStatus());
    assertFalse(order.getDomainEvents().isEmpty());
}

@Test
void shouldNotCompleteOrderIfNotReserved() {
    Order order = Order.create(UserId.of(1), ProductId.of(101), Money.of(100.0));
    
    assertThrows(InvalidOrderStateException.class, order::complete);
}
```

### Tests de Integración

Los tests de integración usan `@EmbeddedKafka` para levantar un broker Kafka in-memory:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, brokerProperties = { 
    "listeners=PLAINTEXT://localhost:9094", 
    "port=9094" 
})
public class OrderServiceIntegrationTest {
    // Tests sin infraestructura externa
}
```

---

## 📝 Notas de Diseño

### ¿Por qué Saga Orquestada vs. Coreografiada?

- **Orquestada** (este proyecto): Un componente central (`OrderSagaOrchestrator`) coordina el flujo. 
  - ✅ Fácil de entender y depurar.
  - ✅ Lógica de compensación centralizada.
  - ⚠️ El orquestador puede ser un punto de fallo.

- **Coreografiada**: Cada servicio escucha eventos y reacciona sin coordinador central.
  - ✅ Más desacoplado.
  - ⚠️ Más difícil de seguir el flujo completo.

### ¿Por qué H2 en lugar de PostgreSQL?

Para simplificar el desarrollo local. En producción se recomienda usar PostgreSQL o similar.

### ¿Por qué Records de Java?

Los DTOs usan `record` de Java para:
- Inmutabilidad automática
- Menos boilerplate
- Ideal para Value Objects y DTOs de transferencia

---

## � Endpoints Disponibles

### Order Service (Puerto 8080)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/orders` | Crear un nuevo pedido (inicia la Saga) |
| GET | `/orders/{id}` | Obtener detalles de un pedido |

### Payment Service (Puerto 8081)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/payments/balance/{userId}` | Consultar saldo de un usuario |

---

## 📄 Licencia

Este proyecto es solo para fines educativos y de demostración.
