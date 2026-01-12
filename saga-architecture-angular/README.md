# 🌐 Saga Architecture - Angular Frontend

Esta es una interfaz moderna desarrollada en **Angular 18** para interactuar con la demo de microservicios Saga. Permite crear pedidos, ver el saldo del usuario en tiempo real y observar los eventos de la saga.

---

## ✨ Características

- **Actualización Automática**: El saldo se actualiza cada 2 segundos mediante polling reactivo con Signals.
- **Saga Log**: Historial en tiempo real de los pasos que sigue la saga (orquestación, pagos, inventario).
- **Diseño Premium**: Interfaz oscura con gradientes, animaciones suaves y micro-interacciones.
- **Signals & Standalone**: Utiliza las últimas características de Angular para un rendimiento óptimo.

---

## 🚀 Inicio Rápido

### Requisitos Previos
- **Node.js 18+**
- **pnpm** (recomendado) o npm

### Instalación

```bash
cd saga-architecture-angular
pnpm install
```

### Ejecutar

```bash
pnpm start
```

La aplicación estará disponible en [http://localhost:4200](http://localhost:4200).

---

## 🛠️ Configuración de Puertos

La aplicación asume que los backends están corriendo en:

| Servicio | Puerto | Proyectos Compatibles |
|----------|--------|----------------------|
| **Order Service** | 8080 | Java / .NET |
| **Payment Service** | 8081 | Java / .NET |
| **Inventory Service** | 8082 | Java / .NET |

---

## 📝 Escenarios de Prueba

1. **Flujo de Éxito**: Crea un pedido para el `productId: 101`. Verás cómo el saldo baja y el log confirma la reserva.
2. **Flujo de Rollback**: Crea un pedido para el `productId: 102`. El log mostrará que no hay stock y el orquestador cancelará el pedido. Verás que el saldo se mantiene (o se revierte si llegó a procesarse).
3. **Flujo de Error de Pago**: Introduce un monto extremadamente alto. El servicio de pagos rechazará la petición por falta de fondos.

---

## 📁 Estructura

- `src/app/services`: Contiene `api.service.ts` con la lógica de Signals y polling.
- `src/app/components`: Componentes desacoplados para Saldo, Formulario y Logs.
- `src/styles.scss`: Estilos globales y sistema de diseño.
