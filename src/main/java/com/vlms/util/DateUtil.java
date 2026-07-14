package com.vlms.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date and time operations throughout the system.
 * Centralizes date logic to avoid duplication (DRY principle).
 */
public final class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    private DateUtil() {
        throw new UnsupportedOperationException("DateUtil is a utility class.");
    }

    /**
     * Returns today's date.
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Returns the current timestamp.
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Checks if a given date is before today (i.e., expired).
     */
    public static boolean isExpired(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * Checks if a given date falls within the next N days.
     */
    public static boolean isExpiringWithinDays(LocalDate date, int days) {
        if (date == null) return false;
        LocalDate today = LocalDate.now();
        long daysUntilExpiry = ChronoUnit.DAYS.between(today, date);
        return daysUntilExpiry >= 0 && daysUntilExpiry <= days;
    }

    /**
     * Returns number of days until the given date (negative if past).
     */
    public static long daysUntil(LocalDate date) {
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    /**
     * Formats a LocalDate to the standard display format.
     */
    public static String format(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DATE_FORMATTER);
    }

    /**
     * Formats a LocalDateTime to the standard display format.
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DATETIME_FORMATTER);
    }
}
