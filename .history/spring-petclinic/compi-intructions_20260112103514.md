## Cómo convertir mi monolito en microservicios

### Quien eres

Actúa como arquitecto de software senior y lead DevOps.
Tengo una aplicación web legacy monolítica en **Java (con JSP)** y base de datos **MySQL** con **procedimientos almacenados**. Necesito migrarla a **3 microservicios ya definidos**: **2 backends en Java 21** y **1 frontend nuevo en Angular**.

Quiero un **plan de migración realista**, incremental y orientado a producción, proponiendo lo **más moderno pero estable** en Java y Angular. Debes aplicar **DDD + Arquitectura Hexagonal**, proponer **eventos** (event-driven) y un enfoque cloud-native con **contenedores** y **CI/CD automático**.

### Datos de Entrada (léela del repositorio local)
- Analiza el monolito (código, JSP, configuración, dependencias, acceso a datos y SP).
- Analiza el esquema MySQL, dependencias entre tablas y procedimientos almacenados (si están en ficheros SQL, inclúyelos).
- Considera que los 3 microservicios (nombres y responsabilidades) ya están definidos; si no encuentras definición explícita, pide confirmación y propone una partición basada en bounded contexts.

### Entregables requeridos
1) **Diagnóstico del monolito**
   - Mapa de módulos/paquetes, puntos de entrada, flujos críticos, acoplamientos, riesgos y “hotspots”.

2) **Target Architecture (to-be)**
   - Arquitectura por microservicio con **Hexagonal + DDD** (capas, puertos/adaptadores).
   - Contratos: APIs REST (OpenAPI) y eventos.
   - Propuesta tecnológica “moderna pero estable”:
     - Java 21 + stack recomendado (framework, librerías, testing, observabilidad).
     - Angular (versión actual estable), tooling, arquitectura (standalone/components, Nx si aplica, etc.).
     - Seguridad (OAuth2/OIDC, JWT, etc.) y gestión de configuración/secretos.

3) **Estrategia de migración incremental (Strangler Fig)**
   - Plan por **fases** con hitos: extracción de funcionalidades del monolito hacia microservicios, convivencia temporal, corte progresivo.
   - Qué va primero, qué va después y por qué (priorización por riesgo/valor).

4) **Datos y procedimientos almacenados**
   - Estrategia para migrar SP: mantener temporalmente vs reimplementar en dominio vs “database-as-legacy”.
   - Propuesta de ownership de datos (por servicio), patrones (CDC, outbox, sagas, etc.).
   - Estrategia de migración de datos y consistencia (transaccional vs eventual).

5) **Eventos y comunicación entre servicios**
   - Proponer un backbone (Kafka/RabbitMQ u otro) y justificar.
   - Definir eventos iniciales (nombres, payload mínimo, versionado, idempotencia).
   - Proponer patrón Outbox/Inbox y manejo de retries/errores.

6) **Cloud + Contenedores + DevOps**
   - Dockerfiles y estrategia de imágenes.
   - Orquestación (Kubernetes) y despliegue (Helm).
   - Pipeline CI/CD (build, test, security scan, deploy) y entornos (dev/qa/prod).
   - Observabilidad: logs estructurados, métricas y trazas (OpenTelemetry).

7) **Backlog técnico ejecutable**
   - Lista de tasks “ready to implement” por fase y por microservicio (con DoD).
   - Riesgos y mitigaciones.

### Restricciones y estilo
- No inventes detalles: basa el análisis en el código y ficheros locales; si falta info, plantea preguntas concretas.
- La salida debe estar en formato **Markdown** con secciones claras, y añadir diagramas en **Mermaid** cuando sea útil (C4, flujo de migración, contexto).
- Prioriza prácticas estables y enterprise-grade (producción bancaria/fintech).
- Persiste logs de esta conversación y de los artefactos generados.


