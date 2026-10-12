package model.annotation;

/**
 * Convenient alias and gateway to {@link util.AnnotationValidator} within the annotation package.
 */
public class AnnotationValidator {

    public static String[] validate(Object obj) {
        return util.AnnotationValidator.validate(obj);
    }

    public static boolean isValid(Object obj) {
        return util.AnnotationValidator.isValid(obj);
    }
}
