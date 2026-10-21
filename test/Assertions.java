package test;

import java.util.Objects;

/**
 * Standard unit test assertion utilities matching JUnit 5 Assertions API.
 */
public class Assertions {

    @FunctionalInterface
    public interface Executable {
        void execute() throws Throwable;
    }

    public static void assertEquals(long expected, long actual, String message) {
        if (expected != actual) {
            throw new AssertionError((message != null ? message + ": " : "") 
                    + "Expected " + expected + " but got " + actual);
        }
    }

    public static void assertEquals(long expected, long actual) {
        assertEquals(expected, actual, null);
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError((message != null ? message + ": " : "") 
                    + "Expected [" + expected + "] but got [" + actual + "]");
        }
    }

    public static void assertEquals(Object expected, Object actual) {
        assertEquals(expected, actual, null);
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message != null ? message : "Expected condition to be true");
        }
    }

    public static void assertTrue(boolean condition) {
        assertTrue(condition, null);
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message != null ? message : "Expected condition to be false");
        }
    }

    public static void assertFalse(boolean condition) {
        assertFalse(condition, null);
    }

    public static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message != null ? message : "Expected object not to be null");
        }
    }

    public static void assertNotNull(Object obj) {
        assertNotNull(obj, null);
    }

    public static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable) {
        try {
            executable.execute();
        } catch (Throwable actualThrown) {
            if (expectedType.isInstance(actualThrown)) {
                return expectedType.cast(actualThrown);
            }
            throw new AssertionError("Expected exception of type " + expectedType.getName() 
                    + " but caught " + actualThrown.getClass().getName(), actualThrown);
        }
        throw new AssertionError("Expected exception of type " + expectedType.getName() + " was NOT thrown.");
    }
}
