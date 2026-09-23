package carwash.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import carwash.exception.DatabaseException;
import carwash.model.Booking;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

// экспорт записей в Excel (.xlsx) через Apache POI
public class ExcelExporter implements Exporter {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void export(List<Booking> bookings, String filePath) {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(filePath)) {

            Sheet sheet = workbook.createSheet("Записи");

            // жирный стиль для шапки
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // шапка таблицы
            String[] headers = {"ID", "Клиент", "Гос. номер", "Модель",
                    "Услуга", "Дата и время", "Цена", "Статус"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // строки с данными
            int rowIndex = 1;
            for (Booking b : bookings) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(b.getId());
                row.createCell(1).setCellValue(b.getClientName() == null ? "" : b.getClientName());
                row.createCell(2).setCellValue(b.getCarNumber());
                row.createCell(3).setCellValue(b.getCarModel() == null ? "" : b.getCarModel());
                row.createCell(4).setCellValue(b.getServiceType().getTitle());
                row.createCell(5).setCellValue(b.getScheduledAt().format(DATE_FORMAT));
                row.createCell(6).setCellValue(b.getPrice());
                row.createCell(7).setCellValue(b.getStatus().getTitle());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
        } catch (IOException e) {
            throw new DatabaseException("Не удалось сохранить Excel-файл: " + filePath, e);
        }
    }

    @Override
    public String getDefaultFileName() {
        return "bookings.xlsx";
    }
}
