# Publisher-change-food-card

## 📋 Описание проекта

**Publisher Change Food Card** — это микросервис на Spring Boot для обработки файлов с данными продуктовых карточек. Сервис поддерживает четыре способа получения файлов, валидацию данных согласно бизнес-правилам, запись в две различные СУБД (PostgreSQL для POM и Oracle для GRU) и автоматическое управление файловой системой.

## 📑 Оглавление

- [Описание проекта](#-описание-проекта)
- [Технологический стек](#-технологический-стек)
- [Архитектура системы](#-архитектура-системы)
- [Как это работает](#-как-это-работает)
  - [Способы приёма файлов](#способы-приёма-файлов)
  - [Жизненный цикл файла](#жизненный-цикл-файла)
  - [Валидация данных](#валидация-данных)
- [API Endpoints](#-api-endpoints)
- [Конфигурация](#-конфигурация)
- [Структура проекта](#-структура-проекта)
- [Запуск проекта](#-запуск-проекта)
- [Тестирование](#-тестирование)

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

## technology 

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
