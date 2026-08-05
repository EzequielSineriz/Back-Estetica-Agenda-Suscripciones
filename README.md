# AppEstetica — Backend

API REST para la gestión de una estética: turnos, clientes y usuarios, con autenticación JWT. Backend del sistema compuesto por la [landing + agenda virtual](https://healthestetica.com) en Angular.

## Stack

- **Java 21** + **Spring Boot 3.5.5**
- **Spring Security** + JWT (`jjwt`) para autenticación stateless
- **Spring Data JPA** + **MySQL** (hosteado en Hostinger)
- **Bean Validation** (`spring-boot-starter-validation`)
- **Lombok**
- Build con **Maven**

## Arquitectura de deploy

```
Angular (Hostinger, healthestetica.com)
        │  HTTPS
        ▼
Spring Boot (Render, backhealthestetica.onrender.com)
        │  JDBC
        ▼
MySQL (Hostinger, srv807.hstgr.io)
```

## Configuración local

### 1. Variables de entorno

Este proyecto **no tiene secretos en `application.properties`** — todo se resuelve por variables de entorno. Creá un archivo `.env` en la raíz (nunca se commitea, ver `.gitignore`) con:

```env
SPRING_DATASOURCE_USERNAME=tu_usuario_mysql
SPRING_DATASOURCE_PASSWORD=tu_password_mysql
JWT_SECRET_KEY=tu_clave_secreta_jwt
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
| `PORT` | Puerto (Render lo inyecta automáticamente) |

## Endpoints principales

### Auth (`/auth`) — públicos

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/auth/register` | Registro de usuario |
| POST | `/auth/login` | Login, devuelve access + refresh token |
| POST | `/auth/refresh-token` | Renueva el access token |

### Clientes (`/api/clients`) — requieren JWT

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/clients` | Listado de clientes |
| POST | `/api/clients` | Crear cliente |
| PUT | `/api/clients/{id}` | Actualizar cliente |
| DELETE | `/api/clients/{id}` | Eliminar cliente |

### Turnos (`/api/appointments`) — requieren JWT

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/appointments` | Listado de turnos |
| GET | `/api/appointments/client/{clientId}` | Turnos de un cliente |
| POST | `/api/appointments` | Crear turno |
| PUT | `/api/appointments/{id}` | Actualizar turno |
| PATCH | `/api/appointments/{id}/complete` | Marcar turno como completado |
| DELETE | `/api/appointments/{id}` | Eliminar turno |

## Seguridad

- Autenticación **stateless** vía JWT (access token + refresh token).
- Roles gestionados con `@EnableMethodSecurity` (`@PreAuthorize`).
- CORS restringido a orígenes específicos (`healthestetica.com`, entornos de desarrollo).
- Los tokens revocados/expirados se registran en la tabla `tokens` para poder invalidarlos en logout.

## Roadmap

- [ ] Sección `/cursos`: catálogo de cursos de la estética.
- [ ] Registro + inscripción a curso en un solo flujo (bajo fricción).
- [ ] Integración de pagos con **MercadoPago** (Checkout Bricks) — soporta Visa, Mastercard y tarjetas de cualquier banco emisor a través de una única integración.
- [ ] Relación `Usuario` ↔ `Curso` vía entidad de inscripción, con estado de pago.
- [ ] Notificaciones de confirmación de pago (webhook de MercadoPago).
