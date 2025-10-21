FROM eclipse-temurin:22-jdk-jammy AS builder

WORKDIR /app

COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .

COPY modules modules

RUN chmod +x gradlew

RUN ./gradlew build -x test --no-daemon

FROM eclipse-temurin:22-jre-jammy

WORKDIR /app

COPY --from=builder /app/modules/bootstrap/api-payment-gateway/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
