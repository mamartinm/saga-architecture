# Prompt: Generación Automática de Tests de Integración para APIs REST con Spring Boot

Actúa como un SDET/Backend senior especialista en Java 21, Spring Boot 3.x, OpenAPI y testing de APIs.

## OBJETIVO

Genera automáticamente una suite de tests de integración (HTTP real) para probar los endpoints REST de este proyecto Spring Boot, usando la especificación OpenAPI como contrato, incluyendo autenticación y autorización (JWT/OAuth2/API Key) según lo definido en `components/securitySchemes` y `security` por operación.

## FUENTES (NO INVENTAR)

- Analiza SOLO el repositorio local.
- Usa como contrato la OpenAPI expuesta por springdoc:
  - **OpenAPI JSON**: `/v3/api-docs`
  - **Swagger UI**: `/swagger-ui.html` (solo como referencia visual)
  - (Si están customizadas, detecta la configuración `springdoc.*` en `application*.yml/properties`)

## STACK DE TEST (OBLIGATORIO)

- JUnit 5
- RestAssured (modo HTTP contra puerto real, `RANDOM_PORT`)
- `spring-boot-starter-test`
- (Opcional si el proyecto usa BD real) Testcontainers con el módulo de Spring Boot testcontainers

## ESTRATEGIA

### 1) Levanta la app en tests con:

- `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`
- Configura RestAssured con el puerto inyectado (`@LocalServerPort`).

### 2) Descarga/lee la OpenAPI:

- `GET http://localhost:{port}/v3/api-docs`
- Parsearla y extraer: paths, métodos, params, requestBodies, responses, content-types, schemas.

### 3) Auth Helper (reutilizable):

- Detecta `securitySchemes`:
  
  **a) http bearer JWT:**
  - Busca en la spec si existe endpoint de login/token (por ejemplo `/auth/login` o `/oauth/token`).
  - Si existe: pide token con credenciales de ENV y cachea el token.
  - Si NO existe: deja "hook" para inyectar un token por ENV (`TEST_JWT`) y marca los tests que lo requieran como skipped si falta.
  
  **b) oauth2:**
  - Usa `tokenUrl` del esquema para obtener `access_token` (client credentials / password grant) con ENV.
  
  **c) apiKey:**
  - Lee header/query name desde la spec y rellena con ENV.

- Soporta al menos 2 identidades:
  - Usuario con permisos (`AUTH_USER_*`)
  - Usuario sin permisos o con scope reducido (`AUTH_USER_LIMITED_*`)
- No loguees tokens ni secretos.

### 4) Generación de tests por operación (mínimo):

- **"Happy path" (2xx)** si request/response están bien definidos.
- **AuthN**: sin credenciales → espera 401 cuando aplique.
- **AuthZ**: con credencial limitada → espera 403 cuando aplique (si la spec o el código permiten inferirlo; si no, documenta la limitación).
- **Validación**: requestBody incompleto (missing required) → 400 cuando aplique.

### 5) Datos de prueba:

- Genera payloads mínimos válidos desde `components/schemas`.
- Si un endpoint depende de otro (ej. `GET/{id}` requiere un recurso creado), crea recurso en `@BeforeEach` y elimina en `@AfterEach` (o usa una estrategia idempotente).

### 6) Estructura de salida (OBLIGATORIA):

- Crea directorio: `./docs/api-tests`
- Genera:
  - `./docs/api-tests/README.md` (cómo ejecutar, ENV necesarias, cómo obtener token)
  - `./docs/api-tests/openapi-coverage.md` (lista de endpoints cubiertos + casos)
  - Tests en `src/test/java/...` siguiendo convención del repo
  - Clase base: `BaseApiIT` con setup RestAssured + obtención de token

## VARIABLES DE ENTORNO (EJEMPLO)

- `BASE_URL` (opcional; por defecto `http://localhost`)
- `AUTH_USERNAME` / `AUTH_PASSWORD` (si hay login)
- `AUTH_USERNAME_LIMITED` / `AUTH_PASSWORD_LIMITED` (para 403)
- `AUTH_CLIENT_ID` / `AUTH_CLIENT_SECRET` (si oauth2 client credentials)
- `TEST_JWT` / `TEST_JWT_LIMITED` (fallback si no hay endpoint de token)
- `API_KEY_VALUE` (si apiKey)
- (si Testcontainers) `DB_IMAGE`, etc.

## CRITERIOS DE ACEPTACIÓN

- Tests reproducibles (no dependen de estado externo).
- Uso de `RANDOM_PORT` y RestAssured apuntando al puerto correcto.
- Suite ejecutable con: `./mvnw test` o `mvn test`
- Si hay endpoints no testeables por falta de credenciales o ambigüedad en la spec, documenta el motivo en `openapi-coverage.md` y marca los tests como `@Disabled` con explicación.

## EMPIEZA

1) Detecta endpoints de OpenAPI (`/v3/api-docs`) y Swagger UI (`/swagger-ui.html`)
2) Genera `BaseApiIT` (JUnit5 + RestAssured + AuthHelper)
3) Genera al menos 1 clase de test por "tag"/resource (por ejemplo `UsersApiIT`, `OrdersApiIT`)
