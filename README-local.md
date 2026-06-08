# SubPilot Local Development

SubPilot is a Spring Boot backend for managing subscriptions, recurring bills, digital assets, reminders, and spending statistics.

## Requirements

- Java 21
- Maven 3.9+
- Docker Desktop

## Start Middleware

```bash
docker compose up -d
docker compose ps
```

Local service URLs:

- MySQL: `localhost:3306`
- Redis: `localhost:6379`
- RabbitMQ Management: `http://localhost:15672` (`guest` / `guest`)
- Elasticsearch: `http://localhost:9200`
- Kibana: `http://localhost:5601`

The MySQL container runs `src/main/resources/db/init.sql` the first time the `mysql_data` volume is created.

## Start Backend

```bash
mvn spring-boot:run
```

Application URLs:

- Health check: `http://localhost:8080/api/health`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Stop Middleware

```bash
docker compose down
```

To reset local middleware data and rerun MySQL initialization:

```bash
docker compose down -v
docker compose up -d
```

## Useful Commands

```bash
mvn clean package
docker compose logs -f mysql
docker compose logs -f redis
docker compose logs -f rabbitmq
docker compose logs -f elasticsearch
```

## Phase 1 Scope

This phase provides the runnable Spring Boot skeleton, unified response and exception handling, Swagger, MyBatis-Plus configuration, Docker Compose middleware, and database initialization SQL. Business modules such as authentication, categories, subscriptions, bills, reminders, and search will be implemented in later phases.
