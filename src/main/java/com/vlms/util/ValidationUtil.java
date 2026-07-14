package com.vlms.util;

/**
 * Utility class for validating common input fields across the system.
 * Ensures consistent validation logic without duplication.
 */
public final class ValidationUtil {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PHONE_REGEX = "^[+]?[0-9]{10,15}$";
    private static final String PAN_REGEX = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$";
    private static final String GST_REGEX = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$";

    private ValidationUtil() {
        throw new UnsupportedOperationException("ValidationUtil is a utility class.");
    }

    public static boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (isNullOrBlank(value)) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank.");
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    public static void requireValidEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches(PHONE_REGEX);
    }

    public static void requireValidPhone(String phone) {
        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException("Invalid phone number format: " + phone);
        }
    }

    public static boolean isValidPan(String pan) {
        return pan != null && pan.matches(PAN_REGEX);
    }

    public static boolean isValidGst(String gst) {
        return gst != null && gst.matches(GST_REGEX);
    }

    public static void requirePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be a positive value. Got: " + value);
        }
    }

    public static void requireInRange(double value, double min, double max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(fieldName + " must be between " + min + " and " + max + ". Got: " + value);
        }
    }
}
