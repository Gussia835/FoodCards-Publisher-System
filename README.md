# Publisher-change-food-card

<details>
  <summary> Нажмите, чтобы посмотреть структуру папок </summary>

```text
src/main/java/org/example/
├── config/          # Конфигурации (Properties, Multi-DataSource)
├── controller/      # Точки входа (REST, gRPC, Scheduled Tasks)
├── service/         # Бизнес-логика (FileReceiverService, SaverDAO)
├── repository/      # Слои доступа к данным (JPA Repositories)
├── models/          # JPA Entity классы (PomFile, GruVistaTab, PomUnit)
├── builders/        # Паттерн Builder для маппинга DTO в Entity
└── utils/           # Утилиты (FileManager, EnrollValidator, EnrollParserVisitor)
```
</details>

## technology 

- Java 21
- Spring Boot 3.2.5 (Web, Data JPA, Scheduling)
- Базы данных: PostgreSQL (POM), Oracle (GRU)
- gRPC 1.60.0 + Protobuf 3.25.1
- Testcontainers 
- Apache Commons Lang3, Datafaker, Lombok
- Gradle, Docker, Docker Compose

## Core Functionality

1. Прием файлов: Поддержка приема через gRPC стримы, REST Multipart или Chunked encoding.
2. Локальное сканирование: Фоновая задача (`@Scheduled`), проверяющая директорию `process-dir` на наличие новых файлов с настраиваемым интервалом (по умолчанию 10 секунд).
3. Обработка: Валидация структуры файла (Header/Body/Trailer), парсинг через паттерн Visitor, атомарное перемещение файлов и сохранение метаданных в PostgreSQL и финансовых записей в Oracle.
4. Автоматическая очистка: Фоновая задача (`@Scheduled` по cron), которая ежедневно (по умолчанию в 02:00) удаляет обработанные директории (`.success`, `.error`) старше заданного количества дней (по умолчанию 30).

## Docker и внешняя конфигурация

Приложение спроектировано для работы в контейнерах. Конфигурационные файлы (application.yml)

# Сборка проекта
./gradlew clean build

# Запуск с внутренними конфигами (для dev-среды)
./gradlew bootRun
