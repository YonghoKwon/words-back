# Docker image

Builds the Spring Boot backend as a local Docker image.

```bash
docker build -t teps-words-back:local .
```

The default Docker profile connects to MySQL on the Mac host:

```text
jdbc:mysql://host.docker.internal:3306/springboot
```

Runtime overrides:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_PROFILES_ACTIVE`

The easiest full-stack local run is from the adjacent `words-front` repo:

```bash
cd ../words-front
docker compose up -d --build
```
