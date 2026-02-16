package com.sajoldev.habittracker.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * HabitDao - Data Access Object
 * Defines all database operations for habits
 */
@Dao
public interface HabitDao {

    /**
     * Get all habits as LiveData for automatic UI updates
     */
    @Query("SELECT * FROM habits ORDER BY id DESC")
    LiveData<List<HabitEntity>> getAllHabits();

    /**
     * Get all habits as a regular list (for background operations)
     */
    @Query("SELECT * FROM habits")
    List<HabitEntity> getAllHabitsSync();

    /**
     * Get a specific habit by ID
     */
    @Query("SELECT * FROM habits WHERE id = :habitId")
    HabitEntity getHabitById(int habitId);

    /**
     * Insert a new habit
     * @return the ID of the newly inserted habit
     */
    @Insert
    long insertHabit(HabitEntity habit);

    /**
     * Update an existing habit
     */
    @Update
    void updateHabit(HabitEntity habit);

    /**
     * Delete a habit
     */
    @Delete
    void deleteHabit(HabitEntity habit);

    /**
     * Delete habit by ID
     */
    @Query("DELETE FROM habits WHERE id = :habitId")
    void deleteHabitById(int habitId);

    /**
     * Update streak information
     */
    @Query("UPDATE habits SET currentStreak = :currentStreak, longestStreak = :longestStreak WHERE id = :habitId")
    void updateStreaks(int habitId, int currentStreak, int longestStreak);

    /**
     * Update completion status
     */
    @Query("UPDATE habits SET isCheckedToday = :isChecked, lastCheckedDate = :lastChecked WHERE id = :habitId")
    void updateCompletionStatus(int habitId, boolean isChecked, long lastChecked);

    /**
     * Get total habit count
     */
    @Query("SELECT COUNT(*) FROM habits")
    int getHabitCount();

    /**
     * Get completed habits count for today
     */
    @Query("SELECT COUNT(*) FROM habits WHERE isCheckedToday = 1")
    int getCompletedHabitsCount();
}
