package com.sajoldev.habittracker.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * DateUtils - Utility class for date operations
 * Centralizes all date formatting and manipulation
 */
public class DateUtils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DISPLAY_FORMAT = "MMM dd";
    public static final String DAY_FORMAT = "EEE";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    private static final SimpleDateFormat displayFormat = new SimpleDateFormat(DISPLAY_FORMAT, Locale.getDefault());
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat(DAY_FORMAT, Locale.getDefault());

    /**
     * Convert Date to string format (yyyy-MM-dd)
     */
    public static String dateToString(Date date) {
        if (date == null) return "";
        return dateFormat.format(date);
    }

    /**
     * Convert string to Date
     */
    public static Date stringToDate(String dateStr) {
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    /**
     * Get today's date as string
     */
    public static String getTodayString() {
        return dateFormat.format(new Date());
    }

    /**
     * Get formatted display date (e.g., "Jan 15")
     */
    public static String getDisplayDate(Date date) {
        return displayFormat.format(date);
    }

    /**
     * Get day name (e.g., "Mon", "Tue")
     */
    public static String getDayName(Date date) {
        return dayFormat.format(date);
    }

    /**
     * Check if two dates are the same day
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Check if date is today
     */
    public static boolean isToday(Date date) {
        return isSameDay(date, new Date());
    }

    /**
     * Add days to a date
     */
    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return cal.getTime();
    }

    /**
     * Get difference in days between two dates
     */
    public static int getDaysDifference(Date date1, Date date2) {
        long diffInMillis = Math.abs(date2.getTime() - date1.getTime());
        return (int) TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Check if a date is yesterday
     */
    public static boolean isYesterday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        return isSameDay(date, cal.getTime());
    }

    /**
     * Get start of day (00:00:00)
     */
    public static Date getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Get end of day (23:59:59)
     */
    public static Date getEndOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    /**
     * Generate array of dates for date picker
     * @param daysBefore Number of days before today to include
     * @param daysAfter Number of days after today to include
     * @return Array of dates
     */
    public static Date[] getDateRange(int daysBefore, int daysAfter) {
        Date[] dates = new Date[daysBefore + daysAfter + 1];
        Calendar cal = Calendar.getInstance();
        
        cal.add(Calendar.DAY_OF_YEAR, -daysBefore);
        for (int i = 0; i < dates.length; i++) {
            dates[i] = cal.getTime();
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        return dates;
    }

    /**
     * Get number of days between two dates
     * @return Number of days (always positive)
     */
    public static long getDaysBetween(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) return 0;
        long diffInMillis = endDate.getTime() - startDate.getTime();
        return Math.max(1, TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS));
    }

    /**
     * Get short day and month format (e.g., "Jan 15")
     */
    public static String getDayMonthShort(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        return sdf.format(date);
    }
}
