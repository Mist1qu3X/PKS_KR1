package ru.mirea.carwash.util;

import ru.mirea.carwash.model.Booking;

import java.util.List;

// общий интерфейс экспорта записей (реализуют ExcelExporter и CsvExporter)
public interface Exporter {

    void export(List<Booking> bookings, String filePath);

    // имя файла по умолчанию для этого формата
    String getDefaultFileName();
}
