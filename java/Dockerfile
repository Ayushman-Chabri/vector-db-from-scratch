# Build stage
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Run stage
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY frontend ./frontend

# host.docker.internal resolves to the host machine on Docker Desktop
# (Windows/Mac) out of the box. On native Linux Docker Engine it needs
# `--add-host=host.docker.internal:host-gateway` at `docker run` time —
# see the run command below.
ENV OLLAMA_HOST=host.docker.internal
ENV OLLAMA_PORT=11434

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]