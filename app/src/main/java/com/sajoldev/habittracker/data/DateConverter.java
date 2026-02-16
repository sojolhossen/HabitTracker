package com.sajoldev.habittracker.data;

import androidx.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * TypeConverters for Room Database
 * Converts complex types (Date, Set<String>) to primitive types for database storage
 */
public class DateConverter {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String fromDateToString(Date date) {
        if (date == null) return null;
        return dateFormat.format(date);
    }

    @TypeConverter
    public static Date fromStringToDate(String value) {
        if (value == null) return null;
        try {
            return dateFormat.parse(value);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}

/**
 * Converter for Set<String> to store in Room Database
 * Stores as comma-separated values
 */
class StringSetConverter {

    @TypeConverter
    public static String fromSet(Set<String> set) {
        if (set == null || set.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String item : set) {
            if (sb.length() > 0) sb.append(",");
            sb.append(item);
        }
        return sb.toString();
    }

    @TypeConverter
    public static Set<String> fromString(String value) {
        Set<String> set = new HashSet<>();
        if (value == null || value.isEmpty()) return set;
        String[] items = value.split(",");
        for (String item : items) {
            if (!item.trim().isEmpty()) {
                set.add(item.trim());
            }
        }
        return set;
    }
}
