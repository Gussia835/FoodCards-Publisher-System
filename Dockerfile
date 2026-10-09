FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

COPY gradle ./gradle
COPY gradlew build.gradle settings.gradle ./
COPY src ./src

RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/Receiver-1.0-SNAPSHOT.jar app.jar

EXPOSE 666 6666

CMD ["java", "-jar", "app.jar"]