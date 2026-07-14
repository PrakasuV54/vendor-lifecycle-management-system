package com.vlms.util;

/**
 * Standardised logger to replace direct System.out/err calls inside services.
 * Keeps output format clean and professional.
 */
public class ConsoleLogger {

    public static void info(String message) {
        System.out.println(message);
    }

    public static void warn(String message) {
        System.out.println("  [WARN] " + message);
    }

    public static void error(String message) {
        System.err.println("  [ERROR] " + message);
    }
}
