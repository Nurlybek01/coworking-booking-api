# Coworking Booking API

Backend-система бронирования рабочих мест и переговорных комнат в коворкинге.

Проект разработан на Java и Spring Boot в рамках семестровой практической работы по дисциплине «Промышленное программирование».

## Технологии

- Java 25
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- PostgreSQL
- Maven
- JUnit
- Lombok

## Требования

Перед запуском необходимо установить:

- JDK 25 или совместимую версию Java;
- Maven 3.9 или выше;
- PostgreSQL 14 или выше;
- IntelliJ IDEA;
- Git.

## Настройка базы данных

Создайте базу данных PostgreSQL:

```sql
CREATE DATABASE coworking_db;
```

Локальная конфигурация находится в файле:

```text
src/main/resources/application.yaml
```

Пример конфигурации:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5432/coworking_db
    username: your_postgres_user
    password: your_postgres_password
```

Не добавляйте реальные пароли в GitHub.

## Запуск проекта

Клонируйте репозиторий:

```bash
git clone <repository-url>
cd coworking-booking-api
```

Запустите тесты:

```bash
mvn clean test
```

Запустите приложение:

```bash
mvn spring-boot:run
```

Приложение будет доступно по адресу:

```text
http://localhost:8080
```

## Структура проекта

```text
src/main/java
└── kz.nurlybek.coworking_booking_api
    ├── controller
    ├── service
    ├── repository
    ├── model
    ├── dto
    ├── exception
    ├── security
    └── report
```

## Роли пользователей

- ADMIN — управление пользователями и ресурсами;
- MANAGER — управление локациями и бронированиями;
- CLIENT — создание и управление собственными бронированиями.

## Основные функции

- управление локациями;
- управление комнатами;
- управление рабочими местами;
- создание бронирований;
- проверка пересечения интервалов;
- расчет стоимости;
- подключение дополнительных услуг;
- обработка оплат;
- отзывы;
- Excel- и Word-отчеты;
- фильтрация, сортировка и пагинация.

## Командная работа

Перед началом работы:

```bash
git pull origin main
```

После изменений:

```bash
git status
git add .
git commit -m "feat: describe your change"
git push origin main
```

Для крупных функций рекомендуется создавать отдельную ветку:

```bash
git checkout -b feature/booking-service
```

После завершения работы создайте Pull Request в ветку `main`.

## Статус проекта

Проект разрабатывается поэтапно. Текущий прогресс:

- [x] Шаг 1.1 — Зависимости (pom.xml)
- [x] Шаг 1.2 — Конфигурация (application.yaml)
- [x] Шаг 1.3 — Логирование (log4j2.xml)
- [x] Шаг 1.4 — Структура пакетов
- [x] Шаг 1.5 — Enum-классы (Role, BookingStatus, PaymentStatus, PaymentMethod, WorkplaceType)
- [x] Шаг 1.6 — Сущности User, Location, Room
- [x] Шаг 1.7 — Сущности Workplace, Tariff, Service
- [x] Шаг 1.8 — Сущности Booking, Payment, Review
- [ ] Шаг 1.9 — Проверка схемы БД

### Что уже работает

- Подключение к PostgreSQL
- JPA/Hibernate с автосозданием схемы (`ddl-auto: update`)
- Логирование через Log4j2 в три файла: `logs/app.log`, `logs/error.log`, `logs/audit.log`
- Сущности User, Location, Room; Hibernate создаёт таблицы и констрейнты автоматически