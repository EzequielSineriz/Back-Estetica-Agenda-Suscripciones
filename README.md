# AppEstetica — Backend

API REST para la gestión de una estética: turnos, clientes, historial clínico, cursos con contenido pago, notificaciones automáticas y usuarios, con autenticación JWT y pagos integrados con MercadoPago. Backend del sistema compuesto por la [landing + agenda virtual](https://healthestetica.com) en Angular.

- Github Frontend: https://github.com/EzequielSineriz/AgendaVirtual-y-Landing

## Stack

- **Java 21** + **Spring Boot 3.5.5**
- **Spring Security** + JWT (`jjwt`) para autenticación stateless, con roles (`ADMIN`/`CUSTOMER`) vía `@PreAuthorize`
- **Spring Data JPA** + **MySQL** (hosteado en Hostinger)
- **MercadoPago SDK** — checkout, preferencias de pago y webhooks con validación de firma HMAC
- **Spring Mail** + **WhatsApp Cloud API (Meta)** — recordatorios automáticos de turnos
- **Spring Scheduling** (`@Scheduled`) — job diario de recordatorios
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
        │  JDBC       │  API+Webhook     │  SMTP        │  Cloud API
        ▼             ▼                  ▼              ▼
      MySQL      MercadoPago          Gmail          WhatsApp (Meta)
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
MAIL_USERNAME=tu_correo@gmail.com
MAIL_PASSWORD=tu_app_password_de_gmail
WHATSAPP_ACCESS_TOKEN=tu_access_token_de_meta
WHATSAPP_PHONE_NUMBER_ID=tu_phone_number_id_de_meta
```

> - Generar una `JWT_SECRET_KEY` nueva: `openssl rand -base64 32`
> - `MAIL_PASSWORD` es una [contraseña de aplicación de Gmail](https://myaccount.google.com/apppasswords) (requiere 2FA activo), no tu contraseña normal.
> - `WHATSAPP_ACCESS_TOKEN`/`WHATSAPP_PHONE_NUMBER_ID` salen del panel de [Meta for Developers](https://developers.facebook.com), dentro de la WhatsApp Business App. Requiere un **template de mensaje aprobado por Meta** (ver sección Notificaciones).

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
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Cuenta de Gmail usada para enviar los recordatorios por email |
| `WHATSAPP_ACCESS_TOKEN` / `WHATSAPP_PHONE_NUMBER_ID` | Credenciales de la WhatsApp Cloud API de Meta |
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

CRUD paginado de clientes + historial de turnos por cliente.

### Turnos (`/api/appointments`) — solo `ADMIN`

CRUD paginado de turnos, con **validación de solapamiento de horarios** y manejo de estados (`PENDING` → `RESERVED` → `CONFIRMED` → `COMPLETED`) según el pago de la seña.

### Historial clínico (`/api/clients/{clientId}/ficha-clinica`, `/api/clients/{clientId}/sesiones`) — solo `ADMIN`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/ficha-clinica` | Ficha fija del cliente (tipo de piel, alergias, medicación, antecedentes) |
| PUT | `/ficha-clinica` | Crea o actualiza la ficha |
| GET | `/sesiones` | Historial de sesiones de tratamiento, más reciente primero |
| POST | `/sesiones` | Registrar una sesión (opcionalmente ligada a un turno, con fotos antes/después) |
| PUT / DELETE | `/sesiones/{id}` | Editar o eliminar una sesión |

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

## Notificaciones automáticas

Un job (`RecordatorioTurnoScheduler`) corre todos los días a las **9:00 AM (America/Argentina/Buenos_Aires)**, busca los turnos de **mañana** que todavía no tengan recordatorio enviado, y dispara:

- **Email**, con un archivo `.ics` adjunto para agendar el turno directo en Google Calendar (u otro calendario compatible).
- **WhatsApp**, vía Meta Cloud API, usando un template de mensaje pre-aprobado (`recordatorio_turno`).

Ambos envíos son best-effort e independientes entre sí: si al cliente le falta el email o el teléfono, simplemente se salta ese canal sin frenar el resto. Un error en un turno puntual no interrumpe el envío del resto de la tanda.

**Requisito externo:** la WhatsApp Cloud API exige que cualquier mensaje iniciado por el negocio use un template aprobado por Meta — la aprobación se gestiona desde el panel de Meta Business y puede tardar horas o días. Sin el template aprobado, el envío de WhatsApp falla (logueado, no rompe el resto del sistema) hasta que se apruebe.

## Seguridad

- Autenticación **stateless** vía JWT (access token + refresh token), ambos persistidos y verificables en la tabla `tokens`.
- Roles gestionados con `@EnableMethodSecurity` (`@PreAuthorize` a nivel de clase en controllers admin-only).
- El registro **nunca** permite auto-asignarse un rol — siempre `CUSTOMER`, promoción a `ADMIN` es manual.
- Rate limiting en `/auth/login` (5 intentos por minuto por IP).
- CORS restringido a orígenes específicos (`healthestetica.com`, subdominios de Vercel/Netlify para entornos de prueba).
- Webhooks de MercadoPago validados criptográficamente (HMAC-SHA256 sobre el manifiesto `id:...;request-id:...;ts:...;`) antes de confirmar cualquier pago.
- `GlobalExceptionHandler` centralizado: excepciones de dominio (`ConflictException`, `BadRequestException`, `ResourceNotFoundException`, `InvalidTokenException`, `ForbiddenException`) mapeadas a status HTTP correctos, con logging completo del stack trace real para debug sin exponer detalles internos al cliente.

## Rendimiento / escalabilidad

- Paginación en los listados de clientes y turnos (`Page<T>` — **breaking change**: el JSON pasó de array plano a `{content, totalElements, totalPages, ...}`, hay que actualizar el frontend Angular para leer `response.content`).
- Índices en las foreign keys más consultadas (`client_id`, `date`, `usuario_id`/`curso_id` de inscripciones).

## Testing

28 tests unitarios (`Mockito` + `AssertJ`, sin dependencia de la base de datos real) sobre la lógica de negocio con más riesgo:

- **`AppointmentServiceTest`** (11): solapamiento de horarios, cálculo de señas, transiciones de estado, idempotencia de `completeAppointment`.
- **`AuthServiceTest`** (8): que el registro nunca asigne un rol distinto a `CUSTOMER`, rechazo de duplicados, validación de refresh token contra tokens revocados.
- **`WebhookSignatureValidatorTest`** (9): validación de firma HMAC con vectores de prueba calculados de forma independiente, rechazo de firmas falsificadas o con datos alterados.

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
- [x] Historial clínico: ficha por cliente + sesiones de tratamiento con fotos.
- [x] Recordatorios automáticos de turnos (Email con `.ics` + WhatsApp).
- [x] Documentación con Swagger.
- [x] Suite de tests unitarios sobre la lógica crítica.
- [x] Manejo de errores globales estandarizado.
- [x] Auditoría de entidades (`AuditableEntity` como `@MappedSuperclass`, timestamps de creación/modificación).
- [x] Señas / pagos parciales para turnos.
- [x] Paginación, rate limiting en login e índices de base de datos.

### Pendiente (técnico)
- [ ] Actualizar el frontend Angular para el nuevo formato paginado de `clients`/`appointments`.
- [ ] Gestión de stock/productos.
- [ ] Panel de administración visual (frontend Angular) — **próximo foco**.

### Backlog de negocio (ideas para cuando haya uso real)
- [ ] **Dashboard de métricas**: ingresos del mes, tasa de no-shows, curso más vendido — convierte el sistema de "agenda" a herramienta de decisión para la dueña.
- [ ] **Política de cancelación de seña**: reglas sobre devolución/retención de la seña según anticipación de la cancelación.
- [ ] **Certificados de curso**: tracking de progreso por módulo (`ProgresoModulo`) y generación automática de PDF al completar el 100%.
- [ ] **Recordatorio de recompra**: detectar tiempo desde la última sesión de un tratamiento y sugerir re-agendar (mismo mecanismo de notificaciones ya construido).
- [ ] **Lista de espera de turnos**: notificar automáticamente si se libera un horario por cancelación.