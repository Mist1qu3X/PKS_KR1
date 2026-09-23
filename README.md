# Автомойка — консольная система записи (КР1)

Консольное приложение на Java: ведёт клиентов и записи на автомойку.
Данные хранятся в PostgreSQL, доступ через JDBC. Архитектура многослойная:

```
Меню (UI)  ->  Сервис (логика)  ->  Репозиторий (JDBC)  ->  PostgreSQL
```

## Возможности
- клиенты: добавить / список / поиск по ID / изменить / удалить;
- записи: создать / список / изменить / сменить статус / удалить;
- поиск (по гос. номеру, по имени клиента);
- фильтрация (по статусу, по услуге, по диапазону дат);
- сортировка (по дате, по цене, по статусу);
- статистика;
- экспорт в Excel (.xlsx) и CSV.

## Что нужно
- JDK 17 или новее;
- PostgreSQL;
- Maven (или IntelliJ IDEA — в ней Maven уже встроен).

## Настройка базы данных
1. Создать базу:
   ```sql
   CREATE DATABASE carwash;
   ```
2. Открыть `src/main/java/carwash/util/DatabaseManager.java` и вписать свои
   параметры подключения (по умолчанию пользователь `postgres`, пароль-заглушка):
   ```java
   private static final String URL = "jdbc:postgresql://localhost:5432/carwash";
   private static final String USER = "postgres";
   private static final String PASSWORD = "your_password"; // ваш пароль
   ```
3. Создать таблицы и тестовые данные можно двумя способами:
   - запустить программу и выбрать пункт меню **7** (выполнит `schema.sql`);
   - или вручную: `psql -U postgres -d carwash -f src/main/resources/schema.sql`.

## Как запустить

### В IntelliJ IDEA
1. Открыть папку проекта (File → Open).
2. При запросе JDK выбрать 17 или новее.
3. Дождаться, пока Maven скачает библиотеки (драйвер PostgreSQL, Apache POI).
4. Запустить `src/main/java/carwash/Main.java` (зелёная стрелка ▶).

### Через Maven
```
mvn clean package
java -jar target/carwash.jar
```
Если в терминале Windows кириллица отображается неправильно, перед запуском:
```
chcp 65001
```

## Структура проекта
```
src/main/java/carwash/
├── Main.java              — точка входа
├── model/                 — Client, Booking, ServiceType, BookingStatus
├── repository/            — работа с БД через JDBC
├── service/               — бизнес-логика и проверки
├── ui/                    — консольное меню и ввод
├── util/                  — подключение к БД, экспорт
└── exception/             — свои исключения
src/main/resources/schema.sql   — создание таблиц и тестовые данные
pom.xml                         — зависимости и сборка
```

## База данных
Две связанные таблицы:
- `clients` — клиенты (id, имя, телефон, email);
- `bookings` — записи на мойку (id, client_id → clients.id, авто, услуга,
  статус, дата, цена).

Ограничения: PRIMARY KEY, FOREIGN KEY, NOT NULL, UNIQUE, CHECK.
