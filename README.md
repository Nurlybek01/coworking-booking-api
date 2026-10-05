# Coworking Booking API

Backend-система бронирования рабочих мест и переговорных комнат в коворкинге.

Проект разработан на Java и Spring Boot.

## Технологии

- Java 25
- Spring Boot 4.1.1
- Spring Web (WebMVC)
- Spring Data JPA / Hibernate
- Spring Security + JWT
- PostgreSQL
- Log4j2
- Apache POI (Excel, Word)
- Maven
- JUnit 5 / Mockito
- Lombok

## Требования

Перед запуском необходимо установить:

- JDK 25 или совместимую версию Java
- Maven 3.9 или выше
- PostgreSQL 14 или выше
- IntelliJ IDEA
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

Не добавляйте реальные пароли в GitHub!

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
    ├── audit            — аудит ключевых действий
    ├── config           — конфигурация Spring
    ├── controller       — REST-контроллеры
    ├── dto              — DTO (request / response)
    ├── exception        — кастомные исключения и GlobalExceptionHandler
    ├── model            — JPA-сущности
    │   └── enums        — перечисления (Role, BookingStatus, PaymentStatus, PaymentMethod, WorkplaceType)
    ├── report           — генерация Excel/Word через Apache POI
    ├── repository       — Spring Data JPA репозитории
    ├── security         — JWT, UserDetailsService, SecurityConfig
    └── service          — бизнес-логика
        └── impl         — реализации сервисов
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

## Схема базы данных

10 таблиц, 6 типов связей и ограничений (1:N, N:M, 1:1, unique, foreign key, check).

| Таблица | Назначение | Ключевые связи |
|---|---|---|
| `users` | Пользователи (ADMIN, MANAGER, CLIENT) | unique (email) |
| `locations` | Локации коворкинга | 1:N → rooms, workplaces |
| `rooms` | Переговорные комнаты | N:1 → locations; check (capacity >= 1) |
| `workplaces` | Рабочие места | N:1 → locations, rooms; unique (location_id, number) |
| `tariffs` | Тарифы | 1:N → bookings |
| `services` | Дополнительные услуги | N:M → bookings |
| `bookings` | Бронирования | N:1 → user/room/workplace/tariff; check (status) |
| `booking_services` | Связь брони и услуг (N:M) | unique (booking_id, service_id) |
| `payments` | Платежи | 1:1 → bookings; unique (booking_id) |
| `reviews` | Отзывы | N:1 → user, room; unique (user_id, room_id); check (rating 1..5) |

**Ключевые индексы:**

- `idx_booking_time` on `bookings(start_time, end_time)` — для быстрого поиска пересечений
- `idx_booking_user` on `bookings(user_id)`

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

### Этап 1 — База данных (ГОТОВО)

- [x] Шаг 1.1 — Зависимости (pom.xml)
- [x] Шаг 1.2 — Конфигурация (application.yaml)
- [x] Шаг 1.3 — Логирование (log4j2.xml)
- [x] Шаг 1.4 — Структура пакетов
- [x] Шаг 1.5 — Enum-классы (Role, BookingStatus, PaymentStatus, PaymentMethod, WorkplaceType)
- [x] Шаг 1.6 — Сущности User, Location, Room
- [x] Шаг 1.7 — Сущности Workplace, Tariff, Service
- [x] Шаг 1.8 — Сущности Booking, Payment, Review
- [x] Шаг 1.9 — Проверка схемы БД (10 таблиц)

### Этап 2 — Репозитории (в работе)

- [ ] Шаг 2.1 — UserRepository, LocationRepository, RoomRepository
- [ ] Шаг 2.2 — WorkplaceRepository, TariffRepository, ServiceRepository
- [ ] Шаг 2.3 — BookingRepository (с поиском доступных ресурсов и статистикой)
- [ ] Шаг 2.4 — PaymentRepository, ReviewRepository

### Этап 3 — Сервисы и DTO

- [ ] DTO и мапперы
- [ ] Кастомные исключения + GlobalExceptionHandler
- [ ] Сервисный слой с @Transactional

### Этап 4 — REST API

- [ ] 20+ REST-endpoint'ов
- [ ] Фильтрация, сортировка, пагинация
- [ ] Валидация запросов

### Этап 5 — Безопасность и JWT

- [ ] Spring Security
- [ ] JWT-аутентификация
- [ ] Роли ADMIN / MANAGER / CLIENT

### Этап 6 — Отчёты (Apache POI)

- [ ] Excel: загрузка помещений и выручка
- [ ] Word: подтверждение бронирования

### Этап 7 — Тесты и логирование

- [ ] 25+ JUnit-тестов бизнес-логики
- [ ] Аудит ключевых действий в `audit.log`

### Что уже работает

- Подключение к PostgreSQL и автосоздание схемы (Hibernate `ddl-auto: update`)
- Логирование через Log4j2 в три файла: `logs/app.log`, `logs/error.log`, `logs/audit.log`
- 9 JPA-сущностей с валидацией, связями и констрейнтами (10 таблиц в БД)
- Проверка инвариантов брони (`end_time > start_time`, наличие ресурса) через `@PrePersist` / `@PreUpdate`