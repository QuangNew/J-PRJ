package com.buseasy.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Consistent date/time formatting for the whole application.
 * Use these methods instead of creating formatters inline.
 */
public class DateUtil {

    private static final DateTimeFormatter DATE_FORMAT      = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_FORMAT      = DateTimeFormatter.ofPattern("HH:mm");

    private DateUtil() {}

    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMAT);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_FORMAT);
    }

    public static String formatTime(LocalDateTime dateTime) {
        return dateTime.format(TIME_FORMAT);
    }
}
