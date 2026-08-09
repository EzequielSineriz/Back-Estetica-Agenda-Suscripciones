# AppEstetica — Backend

API REST para la gestión de una estética: turnos, clientes, cursos con contenido pago y usuarios, con autenticación JWT y pagos integrados con MercadoPago. Backend del sistema compuesto por la [landing + agenda virtual](https://healthestetica.com) en Angular.

- Github Frontend: https://github.com/EzequielSineriz/AgendaVirtual-y-Landing

## Stack

- **Java 21** + **Spring Boot 3.5.5**
- **Spring Security** + JWT (`jjwt`) para autenticación stateless, con roles (`ADMIN`/`CUSTOMER`) vía `@PreAuthorize`
- **Spring Data JPA** + **MySQL** (hosteado en Hostinger)
- **MercadoPago SDK** — checkout, preferencias de pago y webhooks con validación de firma HMAC
- **springdoc-openapi** — documentación interactiva (Swagger UI)
- **Bean Validation** (`spring-boot-starter-validation`)
- **JUnit 5 + Mockito + AssertJ** — suite de tests unitarios sobre la lógica de negocio crítica
- **Lombok**
- Build con **Maven**

## Arquitectura de deploy

```
Angular (Hostinger, healthestetica.com)
        │  HTTPS
        ▼
Spring Boot (Render, backhealthestetica.onrender.com)
        │  JDBC              │  API + Webhook
        ▼                    ▼
MySQL (Hostinger)      MercadoPago
```

## Configuración local

### 1. Variables de entorno

Este proyecto **no tiene secretos en `application.properties`** — todo se resuelve por variables de entorno. Creá un archivo `.env` en la raíz (nunca se commitea, ver `.gitignore`) con:

```env
SPRING_DATASOURCE_USERNAME=tu_usuario_mysql
SPRING_DATASOURCE_PASSWORD=tu_password_mysql
JWT_SECRET_KEY=tu_clave_secreta_jwt
MERCADOPAGO_ACCESS_TOKEN=tu_access_token_de_mercadopago
MERCADOPAGO_WEBHOOK_SECRET=tu_clave_secreta_del_webhook
FRONTEND_BASE_URL=https://healthestetica.com
BACKEND_BASE_URL=https://backhealthestetica.onrender.com
```

> Generar una `JWT_SECRET_KEY` nueva: `openssl rand -base64 32`

Requiere la dependencia [`spring-dotenv`](https://github.com/paulschwarz/spring-dotenv) para cargar el `.env` automáticamente al arrancar.

### 2. Levantar el proyecto

```bash
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

### 3. Tests

```bash
./mvnw test
```

## Variables de entorno requeridas en producción (Render)

| Variable | Descripción |
|---|---|
| `SPRING_DATASOURCE_USERNAME` | Usuario de MySQL |
| `SPRING_DATASOURCE_PASSWORD` | Password de MySQL |
| `JWT_SECRET_KEY` | Clave de firma para los tokens JWT (base64) |
| `MERCADOPAGO_ACCESS_TOKEN` | Access Token de la app de MercadoPago |
| `MERCADOPAGO_WEBHOOK_SECRET` | Clave secreta para validar la firma de los webhooks |
| `FRONTEND_BASE_URL` | URL del frontend, usada en las `back_urls` de MercadoPago |
| `BACKEND_BASE_URL` | URL pública de este backend, usada como `notification_url` |
| `PORT` | Puerto (Render lo inyecta automáticamente) |

## Documentación interactiva (Swagger)

Con el servicio corriendo, la API queda documentada en:
```
{BACKEND_BASE_URL}/swagger-ui.html
```
Botón **"Authorize"** para pegar el JWT y probar los endpoints protegidos directo desde ahí.

## Endpoints principales

### Auth (`/auth`) — públicos

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/auth/register` | Registro de usuario (siempre queda con rol `CUSTOMER`) |
| POST | `/auth/login` | Login, devuelve access + refresh token |
| POST | `/auth/refresh-token` | Renueva el access token (valida contra la tabla `tokens`, no solo la firma del JWT) |

### Clientes (`/api/clients`) — solo `ADMIN`

CRUD completo de clientes + historial de turnos por cliente.

### Turnos (`/api/appointments`) — solo `ADMIN`

CRUD de turnos, con **validación de solapamiento de horarios** y manejo de estados (`PENDING` → `RESERVED` → `CONFIRMED` → `COMPLETED`) según el pago de la seña.

### Cursos (`/api/cursos`)

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| GET | `/api/cursos` | Público | Catálogo de cursos disponibles |
| POST | `/api/cursos` | `ADMIN` | Crear curso |
| POST | `/api/cursos/{id}/inscribirse` | Autenticado | Genera preferencia de pago en MercadoPago |

### Módulos de curso (`/api/cursos/{cursoId}/modulos`, `/api/modulos/{id}`)

Contenido (video + PDF) de cada curso. El `GET` de listado **solo es visible para `ADMIN` o para quien ya pagó ese curso puntual** — se valida contra `InscripcionCurso` con estado `APROBADO`.

### Pagos (`/api/pagos/webhook`) — público, validado por firma

Recibe las notificaciones de MercadoPago (soporta formato legacy `topic`/`id` y el actual `type`/`data.id`, en query params o body), valida el header `x-signature` con HMAC-SHA256 antes de procesar, y nunca propaga excepciones internas (siempre responde `200` para evitar reintentos innecesarios de MercadoPago).

## Seguridad

- Autenticación **stateless** vía JWT (access token + refresh token), ambos persistidos y verificables en la tabla `tokens`.
- Roles gestionados con `@EnableMethodSecurity` (`@PreAuthorize` a nivel de clase en controllers admin-only).
- El registro **nunca** permite auto-asignarse un rol — siempre `CUSTOMER`, promoción a `ADMIN` es manual.
- CORS restringido a orígenes específicos (`healthestetica.com`, subdominios de Vercel/Netlify para entornos de prueba).
- Webhooks de MercadoPago validados criptográficamente (HMAC-SHA256 sobre el manifiesto `id:...;request-id:...;ts:...;`) antes de confirmar cualquier pago.
- `GlobalExceptionHandler` centralizado: excepciones de dominio (`ConflictException`, `BadRequestException`, `ResourceNotFoundException`, `InvalidTokenException`, `ForbiddenException`) mapeadas a status HTTP correctos, con logging completo del stack trace real para debug sin exponer detalles internos al cliente.

## Testing

28 tests unitarios (`Mockito` + `AssertJ`, sin dependencia de la base de datos real) sobre la lógica de negocio con más riesgo:

- **`AppointmentServiceTest`** (11): solapamiento de horarios, cálculo de señas, transiciones de estado, idempotencia de `completeAppointment`.
- **`AuthServiceTest`** (8): que el registro nunca asigne un rol distinto a `CUSTOMER`, rechazo de duplicados, validación de refresh token contra tokens revocados.
- **`WebhookSignatureValidatorTest`** (9): validación de firma HMAC con vectores de prueba calculados de forma independiente (no circular), rechazo de firmas falsificadas o con datos alterados.

```bash
./mvnw test
```

## Roadmap

### Hecho ✅
- [x] Sección `/cursos`: catálogo de cursos de la estética.
- [x] Registro + inscripción a curso en un solo flujo.
- [x] Integración de pagos con **MercadoPago** (Checkout Pro), con validación de firma en el webhook.
- [x] Relación `Usuario` ↔ `Curso` vía `InscripcionCurso`, con estado de pago.
- [x] Módulos de curso (video + PDF) con acceso restringido por compra.
- [x] Documentación con Swagger.
- [x] Suite de tests unitarios sobre la lógica crítica.
- [x] Manejo de errores globales estandarizado.
- [x] Auditoría de entidades (`AuditableEntity`, timestamps de creación/modificación).
- [x] Señas / pagos parciales para turnos.

### Pendiente
- [ ] **Historial clínico / ficha de tratamientos** por cliente (tipo de piel, alergias, observaciones, fotos antes/después).
- [ ] Recordatorios automáticos de turnos (WhatsApp/Email, 24hs antes).
- [ ] Gestión de stock/productos.
- [ ] Panel de administración visual (frontend Angular).
- [ ] Paginación en listados (`clients`, `appointments`, `cursos`) de cara a escalar.
- [ ] Rate limiting en `/auth/login`.
