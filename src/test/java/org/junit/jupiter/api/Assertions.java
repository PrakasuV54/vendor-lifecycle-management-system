package org.junit.jupiter.api;

import java.util.Objects;

public class Assertions {

    public static void assertEquals(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Expected <" + expected + "> but was <" + actual + ">");
        }
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " - Expected <" + expected + "> but was <" + actual + ">");
        }
    }

    public static void assertNotNull(Object actual) {
        if (actual == null) {
            throw new AssertionError("Expected non-null but was null");
        }
    }

    public static void assertNull(Object actual) {
        if (actual != null) {
            throw new AssertionError("Expected null but was <" + actual + ">");
        }
    }

    public static void assertTrue(boolean actual) {
        if (!actual) {
            throw new AssertionError("Expected true but was false");
        }
    }

    public static void assertFalse(boolean actual) {
        if (actual) {
            throw new AssertionError("Expected false but was true");
        }
    }

    public static <T extends Throwable> T assertThrows(Class<T> expectedType, Runnable executable) {
        try {
            executable.run();
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                return expectedType.cast(t);
            }
            throw new AssertionError("Expected exception of type " + expectedType.getName() 
                    + " but caught " + t.getClass().getName(), t);
        }
        throw new AssertionError("Expected exception of type " + expectedType.getName() + " but none was thrown");
    }
}
