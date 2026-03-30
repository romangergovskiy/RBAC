package rbac.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateUtils {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateUtils() {}

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DT);
    }

    public static boolean isBefore(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) < 0;
    }

    public static boolean isAfter(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) > 0;
    }

    public static String addDays(String date, int days) {
        if (date == null || date.length() < 10) return date;
        try {
            LocalDate d = LocalDate.parse(date.substring(0, 10), DATE);
            return d.plusDays(days).format(DATE);
        } catch (Exception e) {
            return date;
        }
    }

    public static String formatRelativeTime(String date) {
        if (date == null || date.length() < 10) return "";
        try {
            LocalDate d = LocalDate.parse(date.substring(0, 10), DATE);
            long diff = ChronoUnit.DAYS.between(LocalDate.now(), d);
            if (diff == 0) return "today";
            if (diff > 0) return "in " + diff + " day" + (diff == 1 ? "" : "s");
            return Math.abs(diff) + " day" + (Math.abs(diff) == 1 ? "" : "s") + " ago";
        } catch (Exception e) {
            return date;
        }
    }
}
