# PageTurn Backend

Spring Boot backend for the PageTurn Android reading app. It provides JWT-based authentication, personal cloud library storage, reading progress sync, bookmarks, highlights, collections, public store APIs, book transfer flows, and scheduled cleanup jobs.

## Tech Stack

- Java 25
- Spring Boot 3.3.x
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven
- Springdoc OpenAPI / Swagger UI

## Features

- Auth with access token + refresh token rotation
- User profile and user search
- Personal cloud library with upload/download
- Reading progress, bookmarks, highlights, and collections
- Offline-first sync endpoints
- Public store with admin upload/update
- Book transfer inbox and accept/decline flow
- Local file storage for uploaded books and covers
- Scheduled cleanup for refresh tokens, transfers, and orphan files

## Project Structure

Main package: `com.pageturn.backend`

Modules:

- `auth`
- `user`
- `security`
- `config`
- `storage`
- `library`
- `progress`
- `bookmark`
- `highlight`
- `collection`
- `sync`
- `transfer`
- `store`
- `scheduler`

## Requirements

Before running the project, install:

- Java 25
- Maven 3.9+
- PostgreSQL 14+ recommended

## Configuration

The app loads base configuration from:

- `src/main/resources/application.yml`
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-prod.yml`

Default server port:

- `8080`

Important configuration values:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_ACCESS_TOKEN_EXPIRATION_MINUTES`
- `JWT_REFRESH_TOKEN_EXPIRATION_DAYS`
- `APP_STORAGE_UPLOAD_DIR`
- `APP_STORAGE_PUBLIC_DIR`
- `APP_STORAGE_MAX_FILE_SIZE`
- `APP_CORS_ORIGIN_ANDROID`
- `APP_CORS_ORIGIN_WEB`
- `APP_CORS_ORIGIN_WEB_ALT`

The application also loads a local `.env` file automatically at startup if it exists in the project root. Real OS environment variables and JVM `-D...` properties still take precedence over `.env`.

Default storage directories:

- uploads: `./var/uploads`
- public files: `./var/public`

Default max upload size:

- `50MB`

## Local Installation

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd pageturn-api
```

### 2. Start PostgreSQL

You can run PostgreSQL with Docker Compose:

```bash
docker compose up -d
```

This starts PostgreSQL on `localhost:5432` with:

- username: `postgres`
- password: `postgres`
- default database: `pageturn_dev`
- extra database: `pageturn`

To stop it:

```bash
docker compose down
```

To remove the persisted database volume too:

```bash
docker compose down -v
```

### 3. Create PostgreSQL databases manually

If you do not want to use Docker, create the databases yourself.

Example:

```sql
CREATE DATABASE pageturn_dev;
CREATE DATABASE pageturn;
```

### 4. Set Java 25

Make sure your shell points to Java 25:

```bash
java -version
echo $JAVA_HOME
```

If needed, export `JAVA_HOME` to your Java 25 installation.

### 5. Configure environment variables

Example for local development:

```bash
export SPRING_PROFILES_ACTIVE=dev
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=pageturn_dev
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=replace-with-a-long-random-secret
export APP_STORAGE_UPLOAD_DIR=./var/dev/uploads
export APP_STORAGE_PUBLIC_DIR=./var/dev/public
export APP_STORAGE_MAX_FILE_SIZE=50MB
```

Or create a `.env` file in the project root:

```dotenv
SPRING_PROFILES_ACTIVE=dev
DB_HOST=localhost
DB_PORT=5432
DB_NAME=pageturn_dev
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=replace-with-a-long-random-secret
APP_STORAGE_UPLOAD_DIR=./var/dev/uploads
APP_STORAGE_PUBLIC_DIR=./var/dev/public
APP_STORAGE_MAX_FILE_SIZE=50MB
```

### 6. Create storage directories

```bash
mkdir -p var/dev/uploads
mkdir -p var/dev/public
```

### 7. Install dependencies and run Flyway migrations

Flyway runs automatically on startup.

### 8. Start the application

Using Maven:

```bash
mvn spring-boot:run
```

Or with the dev profile explicitly:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Production Run

Example:

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=<host>
export DB_PORT=5432
export DB_NAME=<db>
export DB_USERNAME=<user>
export DB_PASSWORD=<password>
export JWT_SECRET=<strong-secret>
export APP_STORAGE_UPLOAD_DIR=/opt/pageturn/uploads
export APP_STORAGE_PUBLIC_DIR=/opt/pageturn/public
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Build

Compile:

```bash
mvn clean compile
```

Run tests:

```bash
mvn test
```

Package:

```bash
mvn clean package
```

## API Documentation

After startup:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI docs: `http://localhost:8080/v3/api-docs`

## Main API Areas

Authentication:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `POST /api/auth/logout-all`
- `GET /api/auth/me`

Users:

- `GET /api/users/me`
- `PATCH /api/users/me`
- `GET /api/users/search`

Library:

- `GET /api/library`
- `POST /api/library`
- `POST /api/library/{bookHash}/upload`
- `GET /api/library/{bookHash}/download`

Progress:

- `PUT /api/progress/{bookHash}`
- `GET /api/progress/{bookHash}`

Bookmarks and highlights:

- `GET /api/books/{bookHash}/bookmarks`
- `POST /api/books/{bookHash}/bookmarks`
- `GET /api/books/{bookHash}/highlights`
- `POST /api/books/{bookHash}/highlights`

Collections:

- `GET /api/collections`
- `POST /api/collections`

Sync:

- `POST /api/sync/push`
- `GET /api/sync/pull`
- `POST /api/sync/deletes`

Transfers:

- `POST /api/transfers`
- `GET /api/transfers/inbox`
- `PUT /api/transfers/{id}/accept`
- `PUT /api/transfers/{id}/decline`

Store:

- `GET /api/store`
- `GET /api/store/{id}`
- `GET /api/store/{id}/download`
- `POST /api/admin/store`
- `PUT /api/admin/store/{id}`

## Security

- Public endpoints include auth register/login/refresh and public store reads
- Protected endpoints require `Authorization: Bearer <accessToken>`
- Admin routes are under `/api/admin/**`

## Scheduled Jobs

The application enables Spring scheduling and includes:

- transfer cleanup
- refresh token cleanup
- orphan file cleanup

## Notes

- File uploads are stored on the local filesystem.
- Firebase is not configured in this backend.
- Flyway is expected to manage schema changes.
- `application-dev.yml` and `application-prod.yml` are both supported.
