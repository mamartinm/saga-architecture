# 🐳 Saga Architecture - Infraestructura

Este directorio contiene la configuración de Docker Compose para levantar la infraestructura necesaria para ejecutar los microservicios de la demo Saga (tanto Java como .NET).

---

## 🚀 Inicio Rápido

### Requisitos Previos
- **Docker** y **Docker Compose** instalados

### Levantar la Infraestructura

```bash
cd saga-architecutra-infra
docker compose up -d
```

### Verificar que todo está corriendo

```bash
docker compose ps
```

Deberías ver:
```
NAME             IMAGE                              STATUS
saga-kafka       confluentinc/cp-kafka:7.5.3        Up (healthy)
saga-kafka-ui    provectuslabs/kafka-ui:latest      Up
saga-zookeeper   confluentinc/cp-zookeeper:7.5.3    Up (healthy)
```

### Detener la Infraestructura

```bash
docker compose down
```

---

## 📦 Servicios Incluidos

| Servicio | Imagen | Puerto | Descripción |
|----------|--------|--------|-------------|
| **Zookeeper** | `confluentinc/cp-zookeeper:7.5.3` | 2181 | Coordinador de Kafka |
| **Kafka** | `confluentinc/cp-kafka:7.5.3` | 9092 | Broker de mensajería |
| **Kafka UI** | `provectuslabs/kafka-ui:latest` | 8300 | Interfaz web para visualizar topics y mensajes |

---

## 🌐 Acceso a Servicios

| Servicio | URL |
|----------|-----|
| **Kafka** (desde microservicios locales) | `localhost:9092` |
| **Kafka UI** | [http://localhost:8300](http://localhost:8300) |
| **Zookeeper** | `localhost:2181` |

---

## 📋 Topics de Kafka

Los microservicios crean automáticamente los siguientes topics:

| Topic | Descripción |
|-------|-------------|
| `order-events` | Eventos de pedidos (ORDER_CREATED, ORDER_COMPLETED, ORDER_CANCELLED) |
| `payment-commands` | Comandos hacia el servicio de pagos |
| `payment-events` | Eventos de pago (PAYMENT_COMPLETED, PAYMENT_FAILED) |
| `inventory-commands` | Comandos hacia el servicio de inventario |
| `inventory-events` | Eventos de inventario (INVENTORY_RESERVED, INVENTORY_REJECTED) |

Puedes visualizar todos los topics y mensajes en **Kafka UI**: [http://localhost:8300](http://localhost:8300)

---

## ⚙️ Configuración Avanzada

### Usar PostgreSQL (Opcional)

El archivo `docker-compose.yml` incluye configuraciones comentadas para usar PostgreSQL en lugar de H2/SQLite. Para habilitarlas:

1. Descomenta las secciones `postgres-order`, `postgres-payment`, `postgres-inventory` y `volumes`.
2. Actualiza la configuración de conexión en los microservicios.

### Puertos PostgreSQL (si se habilita)

| Base de Datos | Puerto |
|---------------|--------|
| orderdb | 5432 |
| paymentdb | 5433 |
| inventorydb | 5434 |

---

## 🔗 Próximos Pasos

1. Lee el README del proyecto que deseas ejecutar:
   - **Java**: `../saga-architecture-java/README.md`
   - **.NET Core**: `../saga-architecture-net/README.md`

2. Ejecuta los microservicios desde tu IDE o terminal.

---

## 📄 Licencia

Este proyecto es solo para fines educativos y de demostración.
