package kz.nurlybek.coworking_booking_api.audit;

public enum AuditAction {
    USER_REGISTERED,
    USER_LOGGED_IN,
    USER_UPDATED,
    BOOKING_CREATED,
    BOOKING_CANCELLED,
    BOOKING_COMPLETED,
    PAYMENT_PAID,
    PAYMENT_REFUNDED,
    REVIEW_CREATED,
    REPORT_GENERATED
}
