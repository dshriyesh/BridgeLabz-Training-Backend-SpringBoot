# fundoo-notes

Production-oriented Spring Boot backend for Fundoo Notes using layered monolithic architecture.

## Tech Stack
- Java 17
- Spring Boot 3.3.x
- Maven
- MySQL
- Spring Security + JWT + Refresh Token
- Redis (token cache, blacklist, OTP)
- RabbitMQ (async email/reminder events)
- Spring Data JPA / Hibernate
- Jakarta Validation
- Lombok
- Spring Actuator
- JUnit

## Implemented Modules
- User Management: register, login, forgot/reset password, profile APIs, profile image upload
- Authentication & Authorization: JWT auth, refresh rotation, logout blacklist, role-based admin access
- Notes Management: create/update/delete/get/list, ownership-safe operations
- Pin/Archive/Trash: toggles + rules + trash timestamp
- Search & Filter: title/date/color/archived/label + sorting + pagination
- Labels: create/update/delete/list
- Reminders & Notifications: reminder create/delete + scheduler + in-app + async email event
- File Attachments: upload/get/delete with type restrictions (`jpg`, `png`, `pdf`)
- Async Operations: RabbitMQ producer/consumer for email/reminder processing
- Token Caching: Redis active token store, blacklist, OTP cache

## Project Setup
1. Start infrastructure.
2. Create a local `.env` file from `.env.example`.
3. Build and run application.

### Local `.env` Setup
Spring Boot is configured to import `fundoo-backend/.env` automatically using:

```properties
spring.config.import=optional:file:.env[.properties]
```

Create your local env file:

```powershell
Copy-Item .env.example .env
```

Then update `.env` with your real secrets.

### Required Environment Variables
At minimum, set these in `.env`:

- `JWT_SECRET`
- `DB_PASSWORD`

Common optional values:

- `DB_URL`
- `DB_USERNAME`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`
- `RABBITMQ_HOST`
- `RABBITMQ_PORT`
- `RABBITMQ_USERNAME`
- `RABBITMQ_PASSWORD`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `APP_PUBLIC_BASE_URL`
- `APP_CORS_ALLOWED_ORIGINS`

Example values are already provided in `C:\Users\Acer\OneDrive\Desktop\FundooNote\fundoo-backend\.env.example:1`.

### Infrastructure (Docker)
```bash
docker compose -f docker-compose.dev.yml up -d
```

For MySQL (if local service not available):
```bash
docker run -d --name fundoo-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=fundoo_notes -p 3306:3306 mysql:8.3
```

## Build Sequence
```bash
mvn clean install
mvn spring-boot:run
```

## Configuration Highlights
- `app.jwt.access-token-expiry-ms=604800000` (7 days)
- `app.jwt.refresh-token-expiry-ms=2592000000` (30 days)
- `app.otp.expiry-seconds=300`
- `app.notes.trash-retention-days=30`
- `app.notes.cleanup.scheduler-delay-ms=3600000`

## API Collections
Postman collection:
- `src/main/resources/postman/fundoo-notes.postman_collection.json`

## SQL Schema
Schema script:
- `src/main/resources/sql/schema.sql`

## Testing
Run tests:
```bash
mvn test
```

## Deployment
### Railway
1. Provision MySQL, Redis, RabbitMQ services.
2. Set environment variables for datasource, JWT secret, Redis, RabbitMQ, and mail.
3. Build command: `mvn clean package -DskipTests`
4. Start command: `java -jar target/fundoo-notes-1.0.0.jar`

### Render
1. Provision managed MySQL/Redis/RabbitMQ.
2. Configure environment variables.
3. Build command: `mvn clean package -DskipTests`
4. Start command: `java -jar target/fundoo-notes-1.0.0.jar`

## Notes
- Use strong secrets in production (`app.jwt.secret`, mail credentials, DB credentials).
- Disable `management.endpoint.health.show-details=always` for public deployments.
- Baseline tests are included; expand service/security integration tests to target 80%+ coverage.
