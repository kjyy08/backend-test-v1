FROM gradle:8.14.3-jdk22 AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY modules modules

RUN gradle build -x test --no-daemon

FROM eclipse-temurin:22-jre-jammy

WORKDIR /app

COPY --from=builder /app/modules/bootstrap/api-payment-gateway/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
