package com.sajoldev.habittracker.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.sajoldev.habittracker.data.HabitDao;
import com.sajoldev.habittracker.data.HabitDatabase;
import com.sajoldev.habittracker.data.HabitEntity;

import java.util.List;

/**
 * HabitRepository - Repository pattern implementation
 * Acts as a single source of truth for all habit data
 * Handles all data operations and provides clean API to ViewModel
 */
public class HabitRepository {

    private HabitDao habitDao;
    private LiveData<List<HabitEntity>> allHabits;

    public HabitRepository(Application application) {
        HabitDatabase database = HabitDatabase.getInstance(application);
        habitDao = database.habitDao();
        allHabits = habitDao.getAllHabits();
    }

    /**
     * Get all habits as LiveData for automatic UI updates
     */
    public LiveData<List<HabitEntity>> getAllHabits() {
        return allHabits;
    }

    /**
     * Get all habits synchronously (for background operations)
     */
    public List<HabitEntity> getAllHabitsSync() {
        return habitDao.getAllHabitsSync();
    }

    /**
     * Insert a new habit
     */
    public void insert(HabitEntity habit) {
        new InsertHabitAsyncTask(habitDao).execute(habit);
    }

    /**
     * Update an existing habit
     */
    public void update(HabitEntity habit) {
        new UpdateHabitAsyncTask(habitDao).execute(habit);
    }

    /**
     * Delete a habit
     */
    public void delete(HabitEntity habit) {
        new DeleteHabitAsyncTask(habitDao).execute(habit);
    }

    /**
     * Delete habit by ID
     */
    public void deleteById(int habitId) {
        new DeleteHabitByIdAsyncTask(habitDao).execute(habitId);
    }

    /**
     * Get habit by ID
     */
    public HabitEntity getHabitById(int habitId) {
        return habitDao.getHabitById(habitId);
    }

    /**
     * Update streak information
     */
    public void updateStreaks(int habitId, int currentStreak, int longestStreak) {
        new UpdateStreaksAsyncTask(habitDao).execute(
                new StreakUpdateData(habitId, currentStreak, longestStreak));
    }

    /**
     * Update completion status
     */
    public void updateCompletionStatus(int habitId, boolean isChecked, long lastChecked) {
        new UpdateCompletionAsyncTask(habitDao).execute(
                new CompletionUpdateData(habitId, isChecked, lastChecked));
    }

    /**
     * Get total habit count
     */
    public int getHabitCount() {
        return habitDao.getHabitCount();
    }

    // AsyncTask classes for database operations

    private static class InsertHabitAsyncTask extends AsyncTask<HabitEntity, Void, Void> {
        private HabitDao habitDao;

        InsertHabitAsyncTask(HabitDao habitDao) {
            this.habitDao = habitDao;
        }

        @Override
        protected Void doInBackground(HabitEntity... habits) {
            habitDao.insertHabit(habits[0]);
            return null;
        }
    }

    private static class UpdateHabitAsyncTask extends AsyncTask<HabitEntity, Void, Void> {
        private HabitDao habitDao;

        UpdateHabitAsyncTask(HabitDao habitDao) {
            this.habitDao = habitDao;
        }

        @Override
        protected Void doInBackground(HabitEntity... habits) {
            habitDao.updateHabit(habits[0]);
            return null;
        }
    }

    private static class DeleteHabitAsyncTask extends AsyncTask<HabitEntity, Void, Void> {
        private HabitDao habitDao;

        DeleteHabitAsyncTask(HabitDao habitDao) {
            this.habitDao = habitDao;
        }

        @Override
        protected Void doInBackground(HabitEntity... habits) {
            habitDao.deleteHabit(habits[0]);
            return null;
        }
    }

    private static class DeleteHabitByIdAsyncTask extends AsyncTask<Integer, Void, Void> {
        private HabitDao habitDao;

        DeleteHabitByIdAsyncTask(HabitDao habitDao) {
            this.habitDao = habitDao;
        }

        @Override
        protected Void doInBackground(Integer... habitIds) {
            habitDao.deleteHabitById(habitIds[0]);
            return null;
        }
    }

    private static class UpdateStreaksAsyncTask extends AsyncTask<StreakUpdateData, Void, Void> {
        private HabitDao habitDao;

        UpdateStreaksAsyncTask(HabitDao habitDao) {
            this.habitDao = habitDao;
        }

        @Override
        protected Void doInBackground(StreakUpdateData... data) {
            habitDao.updateStreaks(data[0].habitId, data[0].currentStreak, data[0].longestStreak);
            return null;
        }
    }

    private static class UpdateCompletionAsyncTask extends AsyncTask<CompletionUpdateData, Void, Void> {
        private HabitDao habitDao;

        UpdateCompletionAsyncTask(HabitDao habitDao) {
            this.habitDao = habitDao;
        }

        @Override
        protected Void doInBackground(CompletionUpdateData... data) {
            habitDao.updateCompletionStatus(data[0].habitId, data[0].isChecked, data[0].lastChecked);
            return null;
        }
    }

    // Helper classes for passing multiple parameters to AsyncTask

    private static class StreakUpdateData {
        int habitId;
        int currentStreak;
        int longestStreak;

        StreakUpdateData(int habitId, int currentStreak, int longestStreak) {
            this.habitId = habitId;
            this.currentStreak = currentStreak;
            this.longestStreak = longestStreak;
        }
    }

    private static class CompletionUpdateData {
        int habitId;
        boolean isChecked;
        long lastChecked;

        CompletionUpdateData(int habitId, boolean isChecked, long lastChecked) {
            this.habitId = habitId;
            this.isChecked = isChecked;
            this.lastChecked = lastChecked;
        }
    }
}
