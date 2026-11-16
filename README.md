# micro_basics

Three Spring Boot microservices showing basic service-to-service communication:

- **FacadeService** — public entry point; receives client requests and fans them out
- **LoggingService** — stores incoming messages
- **MessageService** — returns a static message

Built as a Maven multi-module project.

## Run

```bash
mvn clean install
# then start each service, e.g.
mvn -pl FacadeService spring-boot:run
```

Ports are configured in each service's `src/main/resources/application.properties`.
