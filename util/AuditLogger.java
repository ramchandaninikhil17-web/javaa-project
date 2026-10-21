package util;

import model.annotation.Audit;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;

/**
 * Reflective audit logger that intercepts methods annotated with @Audit
 * and records invocations to logs/audit.log.
 */
public class AuditLogger {

    private static final Path AUDIT_LOG_FILE = Paths.get("logs", "audit.log");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logCall(Class<?> targetClass, String methodName, Class<?>[] paramTypes, Object[] args) {
        try {
            Method method = targetClass.getMethod(methodName, paramTypes);
            if (method.isAnnotationPresent(Audit.class)) {
                Audit audit = method.getAnnotation(Audit.class);
                String action = audit.action().isEmpty() ? methodName : audit.action();
                String timestamp = LocalDateTime.now().format(FORMATTER);
                String argsString = (args != null) ? Arrays.toString(args) : "[]";

                String auditEntry = String.format("[%s] [AUDIT] Action: %s | Method: %s.%s | Params: %s",
                        timestamp, action, targetClass.getSimpleName(), methodName, argsString);

                System.out.println("  " + auditEntry);
                writeAuditToFile(auditEntry);
            }
        } catch (NoSuchMethodException e) {
            System.err.println("Audit logger: method not found: " + methodName);
        }
    }

    private static synchronized void writeAuditToFile(String entry) {
        try {
            if (AUDIT_LOG_FILE.getParent() != null) {
                Files.createDirectories(AUDIT_LOG_FILE.getParent());
            }
            Files.write(
                AUDIT_LOG_FILE,
                Collections.singletonList(entry),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.println("Failed to write to audit log: " + e.getMessage());
        }
    }
}
