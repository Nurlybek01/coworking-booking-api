package kz.nurlybek.coworking_booking_api.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Пишет ключевые действия в отдельный логгер AUDIT (logs/audit.log, см. log4j2.xml). */
@Component
public class AuditLogger {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void log(AuditAction action, String actor, String details) {
        AUDIT.info("action={} actor={} {}", action, actor, details);
    }
}
