package ru.mirea.carwash.ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

// безопасное чтение ввода из консоли: при ошибке повторяем запрос
public class ConsoleReader {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final Scanner scanner = new Scanner(System.in, "UTF-8");

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String readNonEmpty(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Ошибка: значение не может быть пустым.");
        }
    }

    // Enter — оставить текущее значение
    public String readLineOrKeep(String prompt, String current) {
        String value = readLine(prompt);
        return value.isEmpty() ? current : value;
    }

    public int readInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: нужно ввести целое число.");
            }
        }
    }

    // целое число в диапазоне [min; max]
    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            System.out.println("Введите число от " + min + " до " + max + ".");
        }
    }

    public double readDouble(String prompt) {
        while (true) {
            String value = readLine(prompt).replace(",", ".");
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: нужно ввести число.");
            }
        }
    }

    // цена: Enter — взять значение по умолчанию
    public double readPriceWithDefault(String prompt, double defaultValue) {
        while (true) {
            String value = readLine(prompt).replace(",", ".");
            if (value.isEmpty()) {
                return defaultValue;
            }
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: нужно ввести число (или Enter — оставить "
                        + String.format("%.2f", defaultValue) + ").");
            }
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(readLine(prompt), DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Неверная дата. Нужен формат ДД.ММ.ГГГГ (например, 01.10.2026).");
            }
        }
    }

    public LocalTime readTime(String prompt) {
        while (true) {
            try {
                return LocalTime.parse(readLine(prompt), TIME_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Неверное время. Нужен формат ЧЧ:ММ (например, 14:30).");
            }
        }
    }

    // дата и время; повторяем, пока не введут время в будущем
    public LocalDateTime readFutureDateTime() {
        while (true) {
            LocalDate date = readDate("  Дата (ДД.ММ.ГГГГ): ");
            LocalTime time = readTime("  Время (ЧЧ:ММ): ");
            LocalDateTime dateTime = LocalDateTime.of(date, time);
            if (dateTime.isAfter(LocalDateTime.now())) {
                return dateTime;
            }
            System.out.println("  Дата должна быть в будущем. Попробуйте снова.");
        }
    }

    // дата и время для изменения: Enter на дате — оставить текущее
    public LocalDateTime readDateTimeOrKeep(LocalDateTime current) {
        System.out.println("  Текущее: " + current.format(DATE_TIME_FMT)
                + " (Enter на дате — оставить без изменений).");
        String first = readLine("  Новая дата (ДД.ММ.ГГГГ): ");
        if (first.isEmpty()) {
            return current;
        }
        LocalDate date;
        while (true) {
            try {
                date = LocalDate.parse(first, DATE_FMT);
                break;
            } catch (DateTimeParseException e) {
                first = readLine("  Неверная дата. Формат ДД.ММ.ГГГГ: ");
                if (first.isEmpty()) {
                    return current;
                }
            }
        }
        LocalTime time = readTime("  Время (ЧЧ:ММ): ");
        return LocalDateTime.of(date, time);
    }

    // вопрос да/нет
    public boolean confirm(String prompt) {
        String value = readLine(prompt + " (да/нет): ").toLowerCase();
        return value.equals("да") || value.equals("д")
                || value.equals("yes") || value.equals("y");
    }
}
