package ru.mirea.carwash.util;

import ru.mirea.carwash.exception.DatabaseException;
import ru.mirea.carwash.model.Booking;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

// экспорт записей в CSV (разделитель ';', чтобы Excel открывал по столбцам)
public class CsvExporter implements Exporter {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void export(List<Booking> bookings, String filePath) {
        try (PrintWriter writer = new PrintWriter(filePath, StandardCharsets.UTF_8.name())) {
            writer.println("ID;Клиент;Гос. номер;Модель;Услуга;Дата и время;Цена;Статус");
            for (Booking b : bookings) {
                writer.printf("%d;%s;%s;%s;%s;%s;%.2f;%s%n",
                        b.getId(),
                        safe(b.getClientName()),
                        safe(b.getCarNumber()),
                        safe(b.getCarModel()),
                        b.getServiceType().getTitle(),
                        b.getScheduledAt().format(DATE_FORMAT),
                        b.getPrice(),
                        b.getStatus().getTitle());
            }
        } catch (IOException e) {
            throw new DatabaseException("Не удалось сохранить CSV-файл: " + filePath, e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public String getDefaultFileName() {
        return "bookings.csv";
    }
}
