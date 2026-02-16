package com.sajoldev.habittracker.viewmodel;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sajoldev.habittracker.data.HabitEntity;
import com.sajoldev.habittracker.repository.HabitRepository;
import com.sajoldev.habittracker.utils.DateUtils;
import com.sajoldev.habittracker.utils.StreakCalculator;

import java.util.Date;
import java.util.List;

/**
 * HabitViewModel - MVVM ViewModel
 * Handles UI-related data and business logic
 * Survives configuration changes (screen rotation)
 */
public class HabitViewModel extends AndroidViewModel {

    private HabitRepository repository;
    private LiveData<List<HabitEntity>> allHabits;
    private MutableLiveData<Date> selectedDate;

    public HabitViewModel(@NonNull Application application) {
        super(application);
        repository = new HabitRepository(application);
        allHabits = repository.getAllHabits();
        selectedDate = new MutableLiveData<>(new Date());
    }

    /**
     * Get all habits as LiveData
     */
    public LiveData<List<HabitEntity>> getAllHabits() {
        return allHabits;
    }

    /**
     * Insert a new habit
     */
    public void insert(HabitEntity habit) {
        repository.insert(habit);
    }

    /**
     * Update an existing habit
     */
    public void update(HabitEntity habit) {
        repository.update(habit);
    }

    /**
     * Delete a habit
     */
    public void delete(HabitEntity habit) {
        repository.delete(habit);
    }

    /**
     * Delete habit by ID
     */
    public void deleteById(int habitId) {
        repository.deleteById(habitId);
    }

    /**
     * Toggle habit completion status for a specific date
     * This is the main method for checking/unchecking habits
     * 
     * STREAK LOGIC EXPLANATION:
     * 1. When user checks a habit, we add the date to completedDates set
     * 2. We recalculate the current streak by counting consecutive days backwards from today
     * 3. If today is completed, streak = 1 + yesterday's streak (if yesterday was completed)
     * 4. We also update longestStreak if current streak exceeds it
     * 
     * @param habit The habit to toggle
     * @param dateStr Date in "yyyy-MM-dd" format
     * @param isCompleted New completion status
     */
    public void toggleHabitCompletion(HabitEntity habit, String dateStr, boolean isCompleted) {
        if (isCompleted) {
            habit.markCompletedOnDate(dateStr);
        } else {
            habit.unmarkCompletedOnDate(dateStr);
        }

        // Recalculate streaks using StreakCalculator utility
        StreakCalculator.calculateStreaks(habit);

        // Update the habit in database
        repository.update(habit);
    }

    /**
     * Get the currently selected date for viewing
     */
    public LiveData<Date> getSelectedDate() {
        return selectedDate;
    }

    /**
     * Set the selected date for viewing past habits
     */
    public void setSelectedDate(Date date) {
        selectedDate.setValue(date);
    }

    /**
     * Check and reset daily habits at midnight
     * Called when app opens to reset isCheckedToday flag
     * Runs on background thread to avoid blocking UI
     * 
     * MIDNIGHT RESET LOGIC:
     * 1. Compare lastCheckedDate with today's date
     * 2. If dates are different, reset isCheckedToday to false
     * 3. This ensures a fresh start each day while preserving streak history
     */
    public void checkAndResetDailyHabits() {
        new CheckAndResetAsyncTask(repository).execute();
    }

    /**
     * AsyncTask to check and reset habits on background thread
     */
    private static class CheckAndResetAsyncTask extends AsyncTask<Void, Void, Void> {
        private HabitRepository repository;

        CheckAndResetAsyncTask(HabitRepository repository) {
            this.repository = repository;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<HabitEntity> habits = repository.getAllHabitsSync();
            String todayStr = DateUtils.getTodayString();

            for (HabitEntity habit : habits) {
                String lastCheckedStr = DateUtils.dateToString(habit.getLastCheckedDate());

                // If last checked date is not today, reset the daily check
                if (!todayStr.equals(lastCheckedStr)) {
                    habit.setCheckedToday(false);
                    habit.setLastCheckedDate(new Date());
                    repository.update(habit);
                }
            }
            return null;
        }
    }

    /**
     * Get habit count for statistics
     */
    public int getHabitCount() {
        return repository.getHabitCount();
    }
}
