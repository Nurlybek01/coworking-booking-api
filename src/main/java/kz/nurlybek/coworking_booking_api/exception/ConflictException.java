package kz.nurlybek.coworking_booking_api.exception;

/** Запрос корректен, но противоречит текущему состоянию данных (занятый слот, дубликат и т.п.). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
