package com.sajoldev.habittracker.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * HabitDatabase - Room Database
 * Singleton pattern for database instance
 * Version 2: Updated schema after DAO method changes
 */
@Database(entities = {HabitEntity.class}, version = 2, exportSchema = false)
@TypeConverters({DateConverter.class, StringSetConverter.class})
public abstract class HabitDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "habit_tracker_db";
    private static HabitDatabase instance;

    /**
     * Get singleton database instance
     * Thread-safe implementation
     */
    public static synchronized HabitDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    HabitDatabase.class,
                    DATABASE_NAME
            )
            .fallbackToDestructiveMigration() // For development; use proper migration in production
            .build();
        }
        return instance;
    }

    /**
     * Get DAO for habit operations
     */
    public abstract HabitDao habitDao();
}
