package com.company.roombooking.common.util;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class TimeUtils {

    private TimeUtils() {
    }

    public static ZonedDateTime toZonedDateTime(Long epochMillis, ZoneId zoneId) {
        if (epochMillis == null) {
            return null;
        }
        return Instant.ofEpochMilli(epochMillis).atZone(zoneId);
    }

    public static Long toEpochMillis(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return zonedDateTime.toInstant().toEpochMilli();
    }

    /**
     * Checks if the time in minutes is a multiple of stepMinutes.
     */
    public static boolean isStepMultiple(Long epochMillis, int stepMinutes) {
        if (epochMillis == null) {
            return false;
        }
        long minutes = epochMillis / 60000L;
        long remainderSeconds = (epochMillis % 60000L);
        return remainderSeconds == 0 && (minutes % stepMinutes == 0);
    }

    /**
     * Checks if both start and end times fall on working days (Monday - Friday) in the specified zone.
     */
    public static boolean isWorkingDay(Long epochMillis, ZoneId zoneId) {
        if (epochMillis == null) {
            return false;
        }
        ZonedDateTime zdt = toZonedDateTime(epochMillis, zoneId);
        DayOfWeek dow = zdt.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }

    /**
     * Checks if start and end are on the same calendar date in the specified zone.
     */
    public static boolean isSameCalendarDay(Long startMillis, Long endMillis, ZoneId zoneId) {
        if (startMillis == null || endMillis == null) {
            return false;
        }
        ZonedDateTime start = toZonedDateTime(startMillis, zoneId);
        ZonedDateTime end = toZonedDateTime(endMillis, zoneId);
        return start.toLocalDate().isEqual(end.toLocalDate());
    }

    /**
     * Checks if [start, end] falls within daily working hours [workStart, workEnd].
     * Note: Meeting ending exactly at workEnd (e.g. 18:00) is allowed.
     */
    public static boolean isInWorkingHours(Long startMillis, Long endMillis, ZoneId zoneId, LocalTime workStart, LocalTime workEnd) {
        if (startMillis == null || endMillis == null) {
            return false;
        }
        ZonedDateTime start = toZonedDateTime(startMillis, zoneId);
        ZonedDateTime end = toZonedDateTime(endMillis, zoneId);

        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = end.toLocalTime();

        // start must be >= workStart and < workEnd
        if (startTime.isBefore(workStart) || startTime.isAfter(workEnd)) {
            return false;
        }
        // end must be > workStart and <= workEnd
        if (endTime.isBefore(workStart) || endTime.isAfter(workEnd)) {
            return false;
        }
        return true;
    }
}
