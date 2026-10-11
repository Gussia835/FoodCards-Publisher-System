FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

COPY gradle ./gradle
COPY gradlew build.gradle settings.gradle ./
COPY src ./src

RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN mkdir -p /app/config /app/logs /app/incoming /app/target

COPY --from=builder /app/build/libs/Publisher-1.0-SNAPSHOT.jar app.jar

EXPOSE 666 6666

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

ENTRYPOINT ["java", \
            "-Dfile.encoding=UTF-8", \
            "-Dspring.config.additional-location=optional:file:/app/config/application.yml", \
            "-jar", "app.jar"]
