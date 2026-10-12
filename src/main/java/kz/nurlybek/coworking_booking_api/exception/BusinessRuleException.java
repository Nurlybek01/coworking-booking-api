package kz.nurlybek.coworking_booking_api.exception;

/** Нарушение бизнес-правила во входных данных (длительность брони, рабочие часы и т.п.). */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
