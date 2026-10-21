package model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Milestone 3 / Practical 14 Requirement 5: Custom @Audit Annotation
 * 
 * Used to mark sensitive service methods for reflective audit logging.
 * At runtime, the reflective audit logger inspects methods carrying this
 * annotation to log the operation name and execution metadata.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {
    String action() default "";
}
