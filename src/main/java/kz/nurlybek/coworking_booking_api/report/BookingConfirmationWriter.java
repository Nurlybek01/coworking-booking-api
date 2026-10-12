package kz.nurlybek.coworking_booking_api.report;

import kz.nurlybek.coworking_booking_api.dto.BookingResponse;
import kz.nurlybek.coworking_booking_api.dto.ServiceResponse;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/** Word-документ «Подтверждение бронирования». */
@Component
public class BookingConfirmationWriter {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public byte[] write(BookingResponse booking) {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            addTitle(document, "Подтверждение бронирования №" + booking.id());

            String[][] rows = {
                    {"Клиент", booking.userFullName()},
                    {"Локация", booking.locationName() + ", " + booking.locationAddress()},
                    {"Ресурс", resource(booking)},
                    {"Начало", DATE_TIME.format(booking.startTime())},
                    {"Окончание", DATE_TIME.format(booking.endTime())},
                    {"Тариф", booking.tariffName() == null ? "Базовый" : booking.tariffName()},
                    {"Доп. услуги", services(booking)},
                    {"Стоимость", booking.totalPrice().toPlainString()},
                    {"Статус брони", booking.status().name()},
                    {"Статус оплаты", booking.paymentStatus() == null ? "Не оплачено" : booking.paymentStatus().name()}
            };
            XWPFTable table = document.createTable(rows.length, 2);
            for (int i = 0; i < rows.length; i++) {
                table.getRow(i).getCell(0).setText(rows[i][0]);
                table.getRow(i).getCell(1).setText(rows[i][1]);
            }

            document.createParagraph().createRun().setText("Спасибо, что выбрали наш коворкинг!");

            document.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to build booking confirmation", e);
        }
    }

    private static void addTitle(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(16);
        run.setText(text);
    }

    private static String resource(BookingResponse booking) {
        return booking.roomId() != null
                ? "Переговорная «%s»".formatted(booking.roomName())
                : "Рабочее место №" + booking.workplaceNumber();
    }

    private static String services(BookingResponse booking) {
        if (booking.services().isEmpty()) {
            return "—";
        }
        return booking.services().stream()
                .map(ServiceResponse::name)
                .collect(Collectors.joining(", "));
    }
}
