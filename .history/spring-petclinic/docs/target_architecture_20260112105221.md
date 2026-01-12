# Target Architecture (To-Be)

## Propuesta de Microservicios

Basándonos en los contextos delimitados identificados en el monolito de Spring PetClinic, proponemos la siguiente división en 3 microservicios:

1.  **Customer Service (Backend Java 21)**: Gestiona la información de `Owners` y `Pets`.
2.  **Vet Service (Backend Java 21)**: Gestiona la información de `Vets` y `Specialties`.
3.  **Appointment Service (Backend Java 21)**: Gestiona las `Visits` y coordina con `Customer Service` para información de `Pet` y con `Vet Service` para información de `Vet`.
4.  **PetClinic Frontend (Angular)**: Nueva aplicación frontend que consume las APIs de los microservicios.

## Arquitectura por Microservicio (Hexagonal + DDD)

Cada microservicio backend seguirá los principios de la Arquitectura Hexagonal y Domain-Driven Design (DDD).

```mermaid
graph TD
    A[UI / Otros Microservicios] --> B(Application Layer);
    B --> C(Domain Layer);
    C --> D{Ports};
    D -- API REST (Input) --> E[Adaptadores: REST Controller];
    D -- Repositorio (Output) --> F[Adaptadores: JPA Repository];
    D -- Event Publisher (Output) --> G[Adaptadores: Kafka Producer];
    E --> B;
    F --> C;
    G --> C;
    F --> H(Base de Datos);
    G --> I(Kafka Topic);
```

**Capas:**

*   **Application Layer:** Orquesta la lógica de negocio, gestiona transacciones y traduce entre el dominio y los adaptadores.
*   **Domain Layer:** Contiene la lógica de negocio central (Aggregates, Entities, Value Objects, Domain Services, Repositories Interfaces). Es el corazón de la aplicación y es independiente de la tecnología.
*   **Ports:** Interfaces que definen cómo el dominio interactúa con el mundo exterior (ej., `OwnerRepository`, `PetServicePort`).
*   **Adapters:** Implementaciones concretas de los `Ports` que se comunican con tecnologías externas (ej., `JpaOwnerRepository`, `OwnerRestController`, `KafkaEventPublisher`).

## Contratos: APIs REST (OpenAPI) y Eventos

*   **APIs REST:** Cada microservicio expondrá APIs REST bien definidas, siguiendo el estándar OpenAPI (anteriormente Swagger). Esto permitirá una fácil integración con el frontend y otros servicios.
*   **Eventos:** La comunicación asíncrona entre servicios se realizará mediante eventos, utilizando un backbone de eventos como Kafka. Los eventos se definirán con esquemas claros (ej., Avro, JSON Schema) y versiones.

## Propuesta Tecnológica "Moderna pero Estable"

### Backend (Java 21)

*   **Framework:** Spring Boot 3.x (última versión estable).
*   **Lenguaje:** Java 21 LTS.
*   **Persistencia:** Spring Data JPA con Hibernate.
*   **Base de Datos:** PostgreSQL para cada microservicio (bases de datos separadas).
*   **Migraciones de Esquema:** Liquibase o Flyway.
*   **Tolerancia a Fallos:** Resilience4j (Circuit Breaker, Rate Limiter, Retry).
*   **Observabilidad:**
    *   **Métricas:** Micrometer con Prometheus y Grafana.
    *   **Trazas Distribuidas:** OpenTelemetry con Jaeger/Zipkin.
    *   **Logs:** Logback con logs estructurados (JSON) y centralización con ELK Stack (Elasticsearch, Logstash, Kibana) o Grafana Loki.
*   **Testing:** JUnit 5, Mockito, Testcontainers para pruebas de integración.
*   **Build Tool:** Gradle (preferido) o Maven.

### Frontend (Angular)

*   **Framework:** Angular (última versión LTS estable, actualmente Angular 17+).
*   **Arquitectura:** Standalone Components para modularidad. Se podría considerar Nx para gestionar un monorepo si el frontend crece y se divide en micro-frontends.
*   **Tooling:** Angular CLI, ESLint, Prettier.
*   **State Management:** NgRx para aplicaciones complejas o simple servicio RxJS para estados más pequeños.
*   **Styling:** Tailwind CSS o SCSS modular.
*   **Testing:** Karma/Jasmine para unitarias, Cypress para E2E.

### Seguridad

*   **Autenticación/Autorización:** OAuth2/OIDC con un Identity Provider (IdP) como Keycloak o Auth0.
*   **Tokens:** JWT (JSON Web Tokens) para comunicación entre servicios y con el frontend.
*   **API Gateway:** Spring Cloud Gateway (o similar) para centralizar la seguridad, enrutamiento y rate limiting.

### Gestión de Configuración y Secretos

*   **Configuración:** Spring Cloud Config Server o Kubernetes ConfigMaps.
*   **Secretos:** Kubernetes Secrets o HashiCorp Vault para secretos sensibles.

## Diagrama de Arquitectura de Alto Nivel (Contexto)

```mermaid
flowchart LR
%% Sistema PetClinic (To-Be)

user(["Usuario Final<br/>Cliente de la clínica veterinaria o empleado"]);
frontend(["PetClinic Frontend<br/>Aplicación web en Angular para interactuar con los microservicios."]);

identity_provider(["Identity Provider<br/>Keycloak / Auth0 para autenticación y autorización (OAuth2/OIDC)."]);
event_bus(["Event Bus<br/>Apache Kafka para comunicación asíncrona entre servicios."]);
observability(["Plataforma de Observabilidad<br/>Prometheus, Grafana, OpenTelemetry, ELK/Loki."]);

database_customer[("Base de Datos Customer<br/>PostgreSQL para Customer Service.")];
database_vet[("Base de Datos Vet<br/>PostgreSQL para Vet Service.")];
database_appointment[("Base de Datos Appointment<br/>PostgreSQL para Appointment Service.")];

subgraph microservices["Microservicios PetClinic"]
  customer_service(["Customer Service<br/>Gestiona la información de propietarios y mascotas."]);
  vet_service(["Vet Service<br/>Gestiona la información de veterinarios y especialidades."]);
  appointment_service(["Appointment Service<br/>Gestiona las visitas y coordina con otros servicios."]);
end

user -->|Usa| frontend;

frontend -->|Consume API REST| customer_service;
frontend -->|Consume API REST| vet_service;
frontend -->|Consume API REST| appointment_service;

customer_service -->|Lee/Escribe| database_customer;
vet_service -->|Lee/Escribe| database_vet;
appointment_service -->|Lee/Escribe| database_appointment;

customer_service -->|Publica/Consume eventos| event_bus;
vet_service -->|Publica/Consume eventos| event_bus;
appointment_service -->|Publica/Consume eventos| event_bus;

frontend -->|Autenticación| identity_provider;
customer_service -->|Validación de Tokens| identity_provider;
vet_service -->|Validación de Tokens| identity_provider;
appointment_service -->|Validación de Tokens| identity_provider;

customer_service -->|Envía métricas, logs, trazas| observability;
vet_service -->|Envía métricas, logs, trazas| observability;
appointment_service -->|Envía métricas, logs, trazas| observability;


```