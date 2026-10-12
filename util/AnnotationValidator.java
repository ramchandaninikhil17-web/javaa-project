package util;

import model.annotation.MaxLength;
import model.annotation.Positive;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Annotation-driven validator using Java Reflection to inspect runtime object metadata.
 * Automatically checks fields against validation constraints such as @Positive and @MaxLength.
 */
public class AnnotationValidator {

    /**
     * Inspects the specified object using reflection and returns any validation error messages.
     * Checks both declared fields and inherited fields in the class hierarchy.
     *
     * @param obj the object instance to inspect and validate
     * @return an array of human-readable error messages (empty if valid)
     */
    public static String[] validate(Object obj) {
        if (obj == null) {
            return new String[]{"Object to validate cannot be null"};
        }

        List<String> errors = new ArrayList<>();

        // Inspect declared fields on this class and its superclasses (e.g. Account -> SavingsAccount)
        Class<?> currentClass = obj.getClass();
        while (currentClass != null && currentClass != Object.class) {
            Field[] fields = currentClass.getDeclaredFields();

            for (Field field : fields) {
                boolean hasPositive = field.isAnnotationPresent(Positive.class);
                boolean hasMaxLength = field.isAnnotationPresent(MaxLength.class);

                if (!hasPositive && !hasMaxLength) {
                    continue;
                }

                // Grant access to private/protected fields
                field.setAccessible(true);

                try {
                    Object value = field.get(obj);

                    // Validate @Positive constraint
                    if (hasPositive) {
                        Positive positive = field.getAnnotation(Positive.class);
                        if (value instanceof Number number) {
                            if (number.doubleValue() <= 0) {
                                errors.add(field.getName() + " " + positive.message());
                            }
                        }
                    }

                    // Validate @MaxLength constraint
                    if (hasMaxLength) {
                        MaxLength maxLength = field.getAnnotation(MaxLength.class);
                        if (value != null) {
                            String stringVal = value.toString();
                            if (stringVal.length() > maxLength.value()) {
                                errors.add(field.getName() + " " + maxLength.message() + " of " + maxLength.value());
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    errors.add("Unable to access field '" + field.getName() + "': " + e.getMessage());
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        return errors.toArray(new String[0]);
    }

    /**
     * Checks whether an object satisfies all validation rules.
     *
     * @param obj the object to validate
     * @return true if there are no validation errors, false otherwise
     */
    public static boolean isValid(Object obj) {
        return validate(obj).length == 0;
    }
}
