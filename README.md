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

Запустите тесты (PostgreSQL не нужен — тесты используют in-memory H2):

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

### Переменные окружения

| Переменная | Назначение | По умолчанию |
|---|---|---|
| `JWT_SECRET` | Base64-ключ подписи JWT (не короче 256 бит) | dev-ключ, **только для локальной разработки** |
| `ADMIN_EMAIL` | Email первого администратора | `admin@coworking.local` |
| `ADMIN_PASSWORD` | Пароль первого администратора; если не задан — админ не создаётся | — |

Регистрация через API всегда создаёт `CLIENT`. Первый `ADMIN` создаётся при старте из
`ADMIN_EMAIL` / `ADMIN_PASSWORD`, дальше роли назначаются через `PATCH /api/users/{id}`.

## REST API

Все ответы — JSON, ошибки — в формате [Problem Details (RFC 9457)](https://www.rfc-editor.org/rfc/rfc9457).
Защищённые запросы требуют заголовок `Authorization: Bearer <accessToken>`.
Списки поддерживают пагинацию и сортировку: `?page=0&size=20&sort=name,asc` (максимум 100 на страницу).

| Метод | Путь | Доступ | Описание |
|---|---|---|---|
| POST | `/api/auth/register` | все | Регистрация клиента, возвращает токены |
| POST | `/api/auth/login` | все | Вход |
| POST | `/api/auth/refresh` | все | Обновление токенов по refresh-токену |
| GET | `/api/users/me` | авторизованные | Текущий пользователь |
| GET | `/api/users?role=` | ADMIN | Список пользователей |
| GET | `/api/users/{id}` | ADMIN | Пользователь |
| PATCH | `/api/users/{id}` | ADMIN | Смена роли / блокировка |
| GET | `/api/locations?city=` | все | Локации |
| GET / POST / PUT / DELETE | `/api/locations[/{id}]` | чтение — все, изменение — ADMIN, MANAGER | CRUD локаций |
| GET | `/api/rooms?locationId=&minCapacity=` | все | Комнаты |
| GET | `/api/rooms/available?from=&to=` | все | Свободные комнаты на интервал |
| GET / POST / PUT / DELETE | `/api/rooms[/{id}]` | чтение — все, изменение — ADMIN | CRUD комнат |
| GET | `/api/workplaces?locationId=&type=` | все | Рабочие места |
| GET | `/api/workplaces/available?from=&to=` | все | Свободные места на интервал |
| GET / POST / PUT / DELETE | `/api/workplaces[/{id}]` | чтение — все, изменение — ADMIN | CRUD рабочих мест |
| GET / POST / PUT / DELETE | `/api/tariffs[/{id}]` | чтение — все, изменение — ADMIN | CRUD тарифов |
| GET / POST / PUT / DELETE | `/api/services[/{id}]` | чтение — все, изменение — ADMIN | CRUD доп. услуг |
| POST | `/api/bookings` | авторизованные | Создать бронь |
| GET | `/api/bookings?status=&roomId=&workplaceId=&locationId=&userId=&from=&to=` | авторизованные | Брони (клиент видит только свои) |
| GET | `/api/bookings/{id}` | владелец, ADMIN, MANAGER | Бронь |
| POST | `/api/bookings/{id}/cancel` | владелец, ADMIN, MANAGER | Отмена (оплата возвращается) |
| POST | `/api/bookings/{id}/complete` | ADMIN, MANAGER | Завершение |
| POST | `/api/bookings/{id}/payment` | владелец, ADMIN, MANAGER | Оплата |
| GET | `/api/bookings/{id}/payment` | владелец, ADMIN, MANAGER | Платёж по брони |
| GET | `/api/bookings/{id}/confirmation` | владелец, ADMIN, MANAGER | Word-подтверждение брони |
| GET / POST | `/api/rooms/{roomId}/reviews` | чтение — все, отзыв — авторизованные | Отзывы о комнате |
| GET | `/api/reports/occupancy?from=&to=` | ADMIN, MANAGER | Excel: загрузка, выручка, статусы |

DELETE для ресурсов каталога — мягкое удаление (`active = false`): на них ссылаются брони.

### Правила бронирования

- бронируется ровно один ресурс: `roomId` **или** `workplaceId`;
- начало — в будущем, длительность от 30 минут до 12 часов (`app.booking.*`);
- в пределах рабочих часов локации (или 08:00–22:00 по умолчанию);
- пересечение с неотменённой бронью того же ресурса запрещено (409); параллельные запросы
  сериализуются блокировкой строки ресурса;
- стоимость = ставка × длительность (поминутно) + цена доп. услуг; ставка — из тарифа, если он
  указан и действует в этот день, иначе базовая цена ресурса;
- жизненный цикл: `PENDING` → оплата → `CONFIRMED` → `COMPLETED`; отмена из `PENDING`/`CONFIRMED`
  (клиент — только до начала брони);
- отзыв о комнате — только после завершённой брони, один на пользователя.

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

### Этап 2 — Репозитории (ГОТОВО)

- [x] Шаг 2.1 — UserRepository, LocationRepository, RoomRepository
- [x] Шаг 2.2 — WorkplaceRepository, TariffRepository, ServiceRepository
- [x] Шаг 2.3 — BookingRepository (с поиском доступных ресурсов и статистикой)
- [x] Шаг 2.4 — PaymentRepository, ReviewRepository

### Этап 3 — Сервисы и DTO (ГОТОВО)

- [x] DTO и мапперы
- [x] Кастомные исключения + GlobalExceptionHandler
- [x] Сервисный слой с @Transactional

### Этап 4 — REST API (ГОТОВО)

- [x] 20+ REST-endpoint'ов
- [x] Фильтрация, сортировка, пагинация
- [x] Валидация запросов

### Этап 5 — Безопасность и JWT (ГОТОВО)

- [x] Spring Security
- [x] JWT-аутентификация
- [x] Роли ADMIN / MANAGER / CLIENT

### Этап 6 — Отчёты (Apache POI) (ГОТОВО)

- [x] Excel: загрузка помещений и выручка
- [x] Word: подтверждение бронирования

### Этап 7 — Тесты и логирование (ГОТОВО)

- [x] 25+ JUnit-тестов бизнес-логики (59 тестов: unit + `@DataJpaTest` + сквозной MockMvc)
- [x] Аудит ключевых действий в `audit.log`

### Что уже работает

- Подключение к PostgreSQL и автосоздание схемы (Hibernate `ddl-auto: update`)
- Логирование через Log4j2 в три файла: `logs/app.log`, `logs/error.log`, `logs/audit.log`
- 9 JPA-сущностей с валидацией, связями и констрейнтами (10 таблиц в БД)
- Проверка инвариантов брони (`end_time > start_time`, наличие ресурса) через `@PrePersist` / `@PreUpdate`
- REST API с JWT-аутентификацией и ролями, бронирование с проверкой пересечений и расчётом стоимости
- Оплата и возврат, отзывы, Excel- и Word-отчёты
