# Publisher-change-food-card

## Описание проекта

**Publisher Change Food Card** — это микросервис на Spring Boot для обработки файлов с данными продуктовых карточек. Сервис поддерживает четыре способа получения файлов, валидацию данных, запись в две различные СУБД (PostgreSQL для POM и Oracle для GRU) и автоматическое управление файловой системой.

## Оглавление
- [Описание проекта](#описание-проекта)
- [Структура проекта](#структура-проекта)
- [Ключевые возможности](#ключевые-возможности)
- [Технологический стек](#технологический-стек)
- [Основной функционал](#основной-функционал)
- [Жизненный цикл и валидация файла](#жизненный-цикл-и-валидация-файла)
- [API Endpoints](#api-endpoints)
- [Docker и внешняя конфигурация](#docker-и-внешняя-конфигурация)
- [Команды для запуска](#команды-для-запуска)
- [Тестирование](#тестирование)

## Структура проекта
<details>
  <summary> Нажмите, чтобы посмотреть структуру папок </summary>

```text
publisher-change-food-card/
├── src/
│   ├── main/
│   │   ├── java/org/example/
│   │   │   ├── config/
│   │   │   │   ├── GruDataSourceConfig.java       # Конфиг Oracle (изолированный)
│   │   │   │   ├── PomDataSourceConfig.java       # Конфиг PostgreSQL (изолированный)
│   │   │   │   └── AppProperties.java             # Свойства приложения
│   │   │   ├── controller/
│   │   │   │   ├── ChunkController.java           # REST chunks
│   │   │   │   ├── GrpcController.java            # gRPC streaming
│   │   │   │   ├── MultipartController.java       # Multipart upload
│   │   │   │   └── ScanController.java            # File system scanner
│   │   │   ├── models/
│   │   │   │   ├── gru/                           # Entity для Oracle
│   │   │   │   └── pom/                           # Entity для PostgreSQL
│   │   │   ├── repository/
│   │   │   │   ├── gru/                           # Repositories для Oracle
│   │   │   │   └── pom/                           # Repositories для PostgreSQL
│   │   │   ├── service/
│   │   │   │   ├── FileReceiverService.java       # Основной сервис обработки
│   │   │   │   ├── dao/                           # Data Access Objects
│   │   │   │   └── impl/visitor/                  # Реализация паттерна Visitor
│   │   │   ├── exception/                         # Кастомные исключения
│   │   │   └── utils/
│   │   │       ├── Constants.java                 # Константы приложения
│   │   │       └── filename/
│   │   │           ├── FileManager.java           # Управление файлами и папками
│   │   │           └── FileNameUtils.java         # Утилиты для имён файлов
│   │   └── resources/
│   │       ├── application.yml                    # Базовая конфигурация
│   │       └── logback-spring.xml                 # Конфигурация логирования
│   ├── test/                                      # Unit тесты
│   └── integrationTest/                           # Integration тесты (Testcontainers)
├── build.gradle                                   # Gradle build script
├── settings.gradle
└── README.md
```
</details>

### Ключевые возможности:

**4 канала поступления файлов**: файловая система, REST chunks, multipart, gRPC stream  
**Автоматическое управление файлами**: переименование и сортировка по динамическим папкам вида `YYYYMMDD`  
**Двойная база данных**: PostgreSQL (POM) + Oracle (GRU) с изолированными транзакциями  
**Паттерн Visitor**: для гибкого парсинга и валидации данных  
**Внешняя конфигурация**: `logback-spring.xml` и `application.yml` подключаются извне приложения  
**Полное тестирование**: unit + integration tests с использованием Testcontainers

## Технологический стек

### Основные технологии
| Категория | Технология | Версия | Назначение |
|-----------|------------|--------|------------|
| **Framework** | Spring Boot | 3.2.5 | Основной фреймворк |
| **Язык** | Java | 17+ | Язык программирования |
| **Build Tool** | Gradle | 8.x | Сборка проекта |

### Базы данных
| СУБД | Драйвер | Назначение |
|------|---------|------------|
| **PostgreSQL** | 42.7.13 | Хранение POM данных (`PomFile`, `PomUnit`, `PomUnitError`) |
| **Oracle** | 19.23.0.0 | Хранение GRU данных (`GruVistaTab`) |

### Web & API
| Библиотека | Версия | Назначение |
|------------|--------|------------|
| Spring Web | 3.2.5 | REST контроллеры |
| gRPC | 1.60.0 | Streaming API |
| net.devh grpc-spring | 3.0.0.RELEASE | Интеграция gRPC с Spring Boot |
| Protobuf | 3.25.1 | Сериализация для gRPC |

### Обработка файлов и утилиты
| Библиотека | Версия | Назначение |
|------------|--------|------------|
| Apache Tika | 2.9.2 | Определение типов файлов |
| Apache Commons Lang3 | 3.17.0 | Утилиты для работы со строками |
| Lombok | latest | Уменьшение boilerplate кода |
| Spring Data JPA | 3.2.5 | ORM и работа с БД |

### Тестирование
| Библиотека | Версия | Назначение |
|------------|--------|------------|
| Testcontainers | 1.19.3 | Интеграционные тесты с реальными БД |
| JUnit 5 | 5.10.0 | Unit и интеграционные тесты |
| REST Assured | 5.3.2 | Тестирование REST API |
| DataFaker | 2.7.0 | Генерация тестовых данных |

---

## Основной функционал

1. Прием файлов: Поддержка приема через gRPC стримы, REST Multipart или Chunked encoding.
2. Локальное сканирование: Фоновая задача (`@Scheduled`), проверяющая директорию `process-dir` на наличие новых файлов с настраиваемым интервалом (по умолчанию 10 секунд).
3. Обработка: Валидация структуры файла (Header/Body/Trailer), парсинг через паттерн Visitor, атомарное перемещение файлов и сохранение метаданных в PostgreSQL и финансовых записей в Oracle.
4. Автоматическая очистка: Фоновая задача (`@Scheduled` по cron), которая ежедневно (по умолчанию в 02:00) удаляет обработанные директории (`.success`, `.error`) старше заданного количества дней (по умолчанию 30).

## Жизненный цикл и валидация файла
Этапы обработки

1. Получение: Файл сохраняется во временную или процессинговую директорию.
2. Переименование: К имени файла добавляется расширение .in_progress.
3. Валидация:
   - Проверка формата первой строки (Header).
   - Проверка формата последней строки (Trailer).
   - Сверка фактического количества строк в Body с числом, указанным в Trailer.
4. Запись в БД:
   - Успешные строки Body → POM.Unit + GRU.GRU_VISTA_TAB.
   - Ошибочные строки Body → POM.Unit + POM.Unit_ERROR.
   - Если Header или Trailer невалидны, все строки файла считаются ошибочными.
5. Завершение: Файл переименовывается в .success или .error и перемещается в папку YYYYMMDD/success/ или YYYYMMDD/error/.

## API Endpoints
| Метод | Endpoint | Описание | Content-Type |
|-----------|------------|--------|------------|
| POST | /files/chunk | Загрузка файла частями | application/octet-stream |
| POST | /files/multipart | Загрузка файла целиком | multipart/form-data |
| gRPC | GrpcService/upload | Потоковая загрузка (client streaming)  | application/grpc |
                                               



## Docker и внешняя конфигурация

Приложение спроектировано для работы в контейнерах и соответствует принципам 12-Factor App. 
Конфигурационные файлы (application.yml и logback-spring.xml) не зашиты в образ, а могут быть подключены извне через:

- Монтирование volume (например, -v ./config:/config).
- Переменные окружения (environment variables).

Это обеспечивает гибкость настройки под разные среды (dev, stage, prod) без необходимости пересборки образа.

## Команды для запуска
1. Сборка проекта
```bash
./gradlew clean build
```

2. Запуск с внутренними конфигами (для dev-среды)
```bash
./gradlew bootRun
```

3. Запуск с внешними конфигами (для prod-среды)
```bash
java -jar build/libs/publisher-change-food-card-1.0-SNAPSHOT.jar \
  --spring.config.location=file:./config/application.yml
```

## Тестирование

1. Запуск Unit тестов
```bash
./gradlew test
```

2. Запуск Integration тестов (поднимает Testcontainers с PostgreSQL и Oracle)
```bash
./gradlew integrationTest
```
