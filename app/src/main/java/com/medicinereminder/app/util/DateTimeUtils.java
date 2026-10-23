package com.medicinereminder.app.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DISPLAY_TIME_FORMAT = "hh:mm a";
    public static final String DISPLAY_DATE_FORMAT = "EEE, MMM d, yyyy";

    public static String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String formatDate(long millis, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    public static String formatTimeString(String time24) {
        try {
            SimpleDateFormat input = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat(DISPLAY_TIME_FORMAT, Locale.getDefault());
            Date date = input.parse(time24);
            if (date != null) {
                return output.format(date);
            }
        } catch (Exception ignored) {}
        return time24;
    }

    public static String getHeaderDateDisplay(Calendar calendar) {
        Calendar today = Calendar.getInstance();
        if (isSameDay(calendar, today)) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
            return "Today, " + sdf.format(calendar.getTime());
        }

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        if (isSameDay(calendar, tomorrow)) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
            return "Tomorrow, " + sdf.format(calendar.getTime());
        }

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        if (isSameDay(calendar, yesterday)) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
            return "Yesterday, " + sdf.format(calendar.getTime());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Parse time string HH:mm into hours and minutes
     */
    public static int[] parseHourMinute(String timeStr) {
        int[] result = new int[]{8, 0};
        if (timeStr != null && timeStr.contains(":")) {
            String[] parts = timeStr.split(":");
            try {
                result[0] = Integer.parseInt(parts[0].trim());
                result[1] = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException ignored) {}
        }
        return result;
    }

    /**
     * Compute timestamp for given date string and time string
     */
    public static long getMillisForDateTime(String dateStr, String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT + " " + TIME_FORMAT, Locale.getDefault());
            Date date = sdf.parse(dateStr + " " + timeStr);
            if (date != null) return date.getTime();
        } catch (Exception ignored) {}
        return System.currentTimeMillis();
    }
}
