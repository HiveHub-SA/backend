# HiveHub — Backend

The backend service for **HiveHub**, built with Spring Boot. Exposes a REST API, manages business logic, and persists data to a PostgreSQL database.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Web | Spring MVC |
| Persistence | Spring Data JPA + PostgreSQL |
| Security | Spring Security |
| Utilities | Lombok |
| Build Tool | Maven (via Maven Wrapper) |
| Infrastructure | Docker / Docker Compose |

---

## Prerequisites

- **Java 21**
- **Maven 3.x** (or use the included `./mvnw` wrapper)
- **Docker & Docker Compose** (for running the database locally)

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/HiveHub-SA/backend.git
cd backend
```

### 2. Configure environment variables

The database connection relies on the following environment variables. Create a `.env` file at the project root (it is git-ignored):

```env
DB_USER=your_db_user
DB_PASSWORD=your_db_password
DB_NAME=your_db_name
```

Also configure your Spring datasource in `src/main/resources/application.properties` (or `application.yml`):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
```

### 3. Start the database

```bash
docker compose up -d
```

This starts a PostgreSQL 16 container named `hivehub-database` on port `5432`, with data persisted in the `postgres_data` Docker volume.

### 4. Run the application

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080` by default.

---

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/hivehub/      # Application source code
│   │   └── resources/             # Configuration files
│   └── test/                      # Unit and integration tests
├── .mvn/wrapper/                  # Maven wrapper binaries
├── docker-compose.yml             # Local PostgreSQL setup
├── mvnw / mvnw.cmd                # Maven wrapper scripts
└── pom.xml                        # Project dependencies & build config
```

---

## Docker Compose Reference

```yaml
# docker-compose.yml
services:
  postgres-db:
    image: postgres:16-alpine
    container_name: hivehub-database
    ports:
      - "5432:5432"
    environment:
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
      POSTGRES_DB: ${DB_NAME}
    volumes:
      - postgres_data:/var/lib/postgresql/data
```

To stop and remove containers:

```bash
docker compose down
```

To also remove the persistent volume:

```bash
docker compose down -v
```

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## License

This project is currently unlicensed. Contact the HiveHub-SA organization for usage permissions.
