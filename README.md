# Автомойка — система записи (КР1 консоль + КР2 JavaFX)

Приложение на Java для ведения клиентов и записей на автомойку.
Данные хранятся в PostgreSQL, доступ через JDBC. Архитектура многослойная:

```
UI (консоль / JavaFX)  ->  Сервис (логика)  ->  Репозиторий (JDBC)  ->  PostgreSQL
```

- **КР1** — консольный клиент (`carwash.Main`).
- **КР2** — настольный клиент на JavaFX (`carwash.fx.CarWashApp`), FXML + CSS,
  работает с той же базой данных через отдельное JDBC-подключение.

## Возможности
- клиенты: добавить / список / поиск по ID / изменить / удалить;
- записи: создать / список / изменить / сменить статус / удалить;
- поиск (по гос. номеру, по имени клиента);
- фильтрация (по статусу, по услуге, по диапазону дат);
- сортировка (по дате, по цене, по статусу);
- статистика;
- экспорт в Excel (.xlsx) и CSV.

## Что нужно
- JDK 21 (для JavaFX КР2); для КР1 достаточно JDK 17;
- PostgreSQL;
- Maven 3.9+ (или IntelliJ IDEA — в ней Maven уже встроен).

## Настройка базы данных
1. Создать базу:
   ```sql
   CREATE DATABASE carwash;
   ```
2. Указать параметры подключения в `src/main/resources/database.properties`
   (пароль вынесен из кода в этот файл):
   ```properties
   db.url=jdbc:postgresql://localhost:5432/carwash
   db.user=postgres
   db.password=ваш_пароль
   ```
3. Создать таблицы и тестовые данные можно двумя способами:
   - запустить программу и выбрать пункт меню **7** (выполнит `schema.sql`);
   - или вручную: `psql -U postgres -d carwash -f src/main/resources/schema.sql`.

## Как запустить

### КР2 — JavaFX (основное приложение)
В IntelliJ IDEA:
1. Открыть папку проекта (File → Open), выбрать JDK 21.
2. Дождаться загрузки библиотек (JavaFX, драйвер PostgreSQL, Apache POI).
3. Запустить `src/main/java/carwash/fx/CarWashApp.java` (зелёная стрелка ▶),
   либо вкладка **Maven → Plugins → javafx → javafx:run**.

Из командной строки:
```
powershell -ExecutionPolicy Bypass -File .\run.ps1
```
или, если Maven есть в PATH:
```
mvn javafx:run
```

### КР1 — консольный клиент
В IntelliJ запустить `src/main/java/carwash/Main.java`, либо:
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
├── Main.java              — точка входа консоли (КР1)
├── fx/                    — JavaFX: CarWashApp + контроллеры (КР2)
├── model/                 — Client, Booking, ServiceType, BookingStatus
├── repository/            — работа с БД через JDBC (DAO)
├── service/               — бизнес-логика и проверки
├── ui/                    — консольное меню и ввод (КР1)
├── util/                  — подключение к БД, экспорт
└── exception/             — свои исключения
src/main/resources/
├── carwash/fx/            — FXML-разметка и styles.css (КР2)
├── database.properties    — параметры подключения к БД
└── schema.sql             — создание таблиц и тестовые данные
pom.xml                    — зависимости и сборка
```

## База данных
Две связанные таблицы:
- `clients` — клиенты (id, имя, телефон, email);
- `bookings` — записи на мойку (id, client_id → clients.id, авто, услуга,
  статус, дата, цена).

Ограничения: PRIMARY KEY, FOREIGN KEY, NOT NULL, UNIQUE, CHECK.
