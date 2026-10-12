package kz.nurlybek.coworking_booking_api.report;

import kz.nurlybek.coworking_booking_api.model.enums.BookingStatus;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;

@Component
public class ExcelReportWriter {

    // Фиксированная ширина вместо autoSizeColumn: тот требует системных шрифтов AWT,
    // которых может не быть в headless-контейнере.
    private static final int COLUMN_WIDTH = 28 * 256;

    public byte[] write(OccupancyReport report) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle header = headerStyle(workbook);
            CellStyle money = moneyStyle(workbook);

            writeUsage(workbook, report, header, money);
            writeRevenue(workbook, report, header, money);
            writeStatuses(workbook, report, header);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build Excel report", e);
        }
    }

    private void writeUsage(Workbook workbook, OccupancyReport report, CellStyle header, CellStyle money) {
        Sheet sheet = createSheet(workbook, "Загрузка", header, report,
                "Локация", "Ресурс", "Броней", "Часов");
        int rowIndex = 3;
        for (OccupancyReport.ResourceUsage usage : report.usage()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(usage.location());
            row.createCell(1).setCellValue(usage.resource());
            row.createCell(2).setCellValue(usage.bookings());
            setNumber(row, 3, usage.hours(), money);
        }
    }

    private void writeRevenue(Workbook workbook, OccupancyReport report, CellStyle header, CellStyle money) {
        Sheet sheet = createSheet(workbook, "Выручка", header, report, "Локация", "Выручка");
        int rowIndex = 3;
        BigDecimal total = BigDecimal.ZERO;
        for (OccupancyReport.LocationRevenue revenue : report.revenue()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(revenue.location());
            setNumber(row, 1, revenue.amount(), money);
            total = total.add(revenue.amount());
        }
        Row totalRow = sheet.createRow(rowIndex);
        totalRow.createCell(0).setCellValue("Итого");
        totalRow.getCell(0).setCellStyle(header);
        setNumber(totalRow, 1, total, money);
    }

    private void writeStatuses(Workbook workbook, OccupancyReport report, CellStyle header) {
        Sheet sheet = createSheet(workbook, "Статусы", header, report, "Статус", "Количество");
        int rowIndex = 3;
        for (BookingStatus status : BookingStatus.values()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(status.name());
            row.createCell(1).setCellValue(report.statusCounts().getOrDefault(status, 0L));
        }
    }

    private Sheet createSheet(Workbook workbook, String name, CellStyle header,
                              OccupancyReport report, String... columns) {
        Sheet sheet = workbook.createSheet(name);
        sheet.createRow(0).createCell(0).setCellValue("Период: %s — %s".formatted(report.from(), report.to()));
        Row headerRow = sheet.createRow(2);
        for (int i = 0; i < columns.length; i++) {
            headerRow.createCell(i).setCellValue(columns[i]);
            headerRow.getCell(i).setCellStyle(header);
            sheet.setColumnWidth(i, COLUMN_WIDTH);
        }
        return sheet;
    }

    private static void setNumber(Row row, int column, BigDecimal value, CellStyle style) {
        row.createCell(column).setCellValue(value.doubleValue());
        row.getCell(column).setCellStyle(style);
    }

    private static CellStyle headerStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private static CellStyle moneyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }
}
