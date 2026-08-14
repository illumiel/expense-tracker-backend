# ---------- Stage 1: Build ----------
# Maven + JDK 17 image compiles the project and packages it into a fat jar
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the POM first and fetch dependencies in a separate layer, so that
# dependency downloads are cached and only re-run when pom.xml changes
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy the source code and build the jar (skipping tests for speed)
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: Runtime ----------
# Lightweight JRE-only image - no Maven, no compiler, no JDK
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Run as a non-root user (production best practice)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy only the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

USER appuser

# Render/cloud hosts use a dynamic PORT env var; the app reads it via
# server.port=${PORT:8080} in application.properties
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]