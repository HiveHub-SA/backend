# Stage 1: Build
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x mvnw && ./mvnw clean package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/app-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
