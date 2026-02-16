package com.sajoldev.habittracker.utils;

import com.sajoldev.habittracker.data.HabitEntity;

import java.util.Calendar;
import java.util.Date;

/**
 * StreakCalculator - Utility class for calculating habit streaks
 * 
 * STREAK CALCULATION LOGIC:
 * =========================
 * 
 * Current Streak: Number of consecutive days the habit was completed,
 * counting backwards from today (or the most recent completed day).
 * 
 * Example:
 * - Today: Jan 15 (completed) → Streak = 1
 * - Yesterday: Jan 14 (completed) → Streak = 2
 * - Day before: Jan 13 (NOT completed) → Streak stops
 * 
 * Longest Streak: The highest streak count ever achieved for this habit
 * We keep track of this separately and update it whenever current streak exceeds it.
 * 
 * Edge Cases Handled:
 * 1. If today is not completed but yesterday was, streak = yesterday's streak
 * 2. If there's a gap in completion, streak resets to 0
 * 3. If today is completed, streak = 1 + yesterday's streak
 */
public class StreakCalculator {

    /**
     * Calculate both current and longest streaks for a habit
     * Updates the habit object directly
     * 
     * @param habit The habit to calculate streaks for
     */
    public static void calculateStreaks(HabitEntity habit) {
        int currentStreak = calculateCurrentStreak(habit);
        habit.setCurrentStreak(currentStreak);

        // Update longest streak if current streak is higher
        if (currentStreak > habit.getLongestStreak()) {
            habit.setLongestStreak(currentStreak);
        }
    }

    /**
     * Calculate current streak by counting consecutive completed days
     * going backwards from today
     * 
     * ALGORITHM:
     * 1. Start from today
     * 2. Check if date is in completedDates set
     * 3. If yes, increment streak and check previous day
     * 4. If no, stop counting (streak is broken)
     * 5. Return total count
     * 
     * @param habit The habit to check
     * @return Number of consecutive days completed (0 if not completed today)
     */
    public static int calculateCurrentStreak(HabitEntity habit) {
        if (habit.getCompletedDates() == null || habit.getCompletedDates().isEmpty()) {
            return 0;
        }

        int streak = 0;
        Calendar cal = Calendar.getInstance();
        
        // Start from today and go backwards
        // Note: We don't require today to be completed to maintain streak from yesterday
        // This handles the case where user checks habits in the morning
        
        // First check if we should count today
        String todayStr = DateUtils.getTodayString();
        boolean countToday = habit.isCompletedOnDate(todayStr);
        
        if (countToday) {
            streak = 1;
            cal.add(Calendar.DAY_OF_YEAR, -1); // Move to yesterday
        }
        
        // Count backwards from yesterday (or today if not completed)
        while (true) {
            String dateStr = DateUtils.dateToString(cal.getTime());
            
            if (habit.isCompletedOnDate(dateStr)) {
                streak++;
                cal.add(Calendar.DAY_OF_YEAR, -1); // Go to previous day
            } else {
                // Streak is broken
                break;
            }
            
            // Safety limit to prevent infinite loop
            if (streak > 3650) { // 10 years max
                break;
            }
        }
        
        return streak;
    }

    /**
     * Calculate streak up to a specific date
     * Useful for viewing past progress
     * 
     * @param habit The habit to check
     * @param upToDate Calculate streak up to this date (inclusive)
     * @return Streak count up to the specified date
     */
    public static int calculateStreakUpToDate(HabitEntity habit, Date upToDate) {
        if (habit.getCompletedDates() == null || habit.getCompletedDates().isEmpty()) {
            return 0;
        }

        int streak = 0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(upToDate);

        while (true) {
            String dateStr = DateUtils.dateToString(cal.getTime());
            
            if (habit.isCompletedOnDate(dateStr)) {
                streak++;
                cal.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
            
            if (streak > 3650) {
                break;
            }
        }
        
        return streak;
    }

    /**
     * Check if a habit should maintain its streak for today
     * A habit maintains streak if:
     * 1. Today is completed, OR
     * 2. Today is not yet over AND yesterday was completed
     * 
     * This prevents streak loss if user hasn't checked the app yet today
     * 
     * @param habit The habit to check
     * @return true if streak should be maintained
     */
    public static boolean shouldMaintainStreak(HabitEntity habit) {
        String todayStr = DateUtils.getTodayString();
        
        // If completed today, definitely maintain
        if (habit.isCompletedOnDate(todayStr)) {
            return true;
        }
        
        // Check if yesterday was completed (gives user grace period until end of today)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterdayStr = DateUtils.dateToString(cal.getTime());
        
        return habit.isCompletedOnDate(yesterdayStr);
    }

    /**
     * Get completion rate for the last N days
     * 
     * @param habit The habit to check
     * @param days Number of days to look back
     * @return Percentage of days completed (0-100)
     */
    public static int getCompletionRate(HabitEntity habit, int days) {
        if (habit.getCompletedDates() == null || days <= 0) {
            return 0;
        }

        int completedDays = 0;
        Calendar cal = Calendar.getInstance();
        
        for (int i = 0; i < days; i++) {
            String dateStr = DateUtils.dateToString(cal.getTime());
            if (habit.isCompletedOnDate(dateStr)) {
                completedDays++;
            }
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        
        return (completedDays * 100) / days;
    }
}
