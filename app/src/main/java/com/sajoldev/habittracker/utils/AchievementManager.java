package com.sajoldev.habittracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.sajoldev.habittracker.data.HabitEntity;

import java.util.ArrayList;
import java.util.List;

public class AchievementManager {

    private static final String PREFS_NAME = "achievements";
    private SharedPreferences prefs;

    public static final String FIRST_HABIT = "first_habit";
    public static final String STREAK_3_DAYS = "streak_3_days";
    public static final String STREAK_7_DAYS = "streak_7_days";
    public static final String STREAK_30_DAYS = "streak_30_days";
    public static final String PERFECT_WEEK = "perfect_week";
    public static final String EARLY_BIRD = "early_bird";
    public static final String NIGHT_OWL = "night_owl";
    public static final String COMPLETION_10 = "completion_10";
    public static final String COMPLETION_50 = "completion_50";
    public static final String COMPLETION_100 = "completion_100";

    public AchievementManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void checkAchievements(Context context, List<HabitEntity> habits) {
        if (habits.size() >= 1 && !isUnlocked(FIRST_HABIT)) {
            unlock(FIRST_HABIT);
        }

        int totalCompletions = 0;
        int bestStreak = 0;
        boolean hasPerfectWeek = false;

        for (HabitEntity habit : habits) {
            totalCompletions += habit.getCompletionDatesList().size();
            if (habit.getCurrentStreak() > bestStreak) {
                bestStreak = habit.getCurrentStreak();
            }
            if (habit.getCurrentStreak() >= 7) {
                hasPerfectWeek = true;
            }
        }

        if (bestStreak >= 3 && !isUnlocked(STREAK_3_DAYS)) unlock(STREAK_3_DAYS);
        if (bestStreak >= 7 && !isUnlocked(STREAK_7_DAYS)) unlock(STREAK_7_DAYS);
        if (bestStreak >= 30 && !isUnlocked(STREAK_30_DAYS)) unlock(STREAK_30_DAYS);
        if (hasPerfectWeek && !isUnlocked(PERFECT_WEEK)) unlock(PERFECT_WEEK);

        if (totalCompletions >= 10 && !isUnlocked(COMPLETION_10)) unlock(COMPLETION_10);
        if (totalCompletions >= 50 && !isUnlocked(COMPLETION_50)) unlock(COMPLETION_50);
        if (totalCompletions >= 100 && !isUnlocked(COMPLETION_100)) unlock(COMPLETION_100);
    }

    private void unlock(String achievement) {
        prefs.edit().putBoolean(achievement, true).apply();
    }

    public boolean isUnlocked(String achievement) {
        return prefs.getBoolean(achievement, false);
    }

    public List<Achievement> getAllAchievements() {
        List<Achievement> achievements = new ArrayList<>();
        achievements.add(new Achievement(FIRST_HABIT, "Getting Started", "Create your first habit", "🌱"));
        achievements.add(new Achievement(STREAK_3_DAYS, "On Fire!", "3 day streak", "🔥"));
        achievements.add(new Achievement(STREAK_7_DAYS, "Week Warrior", "7 day streak", "⚡"));
        achievements.add(new Achievement(STREAK_30_DAYS, "Habit Master", "30 day streak", "👑"));
        achievements.add(new Achievement(PERFECT_WEEK, "Perfect Week", "Complete all habits for a week", "⭐"));
        achievements.add(new Achievement(COMPLETION_10, "Beginner", "Complete habits 10 times", "🎯"));
        achievements.add(new Achievement(COMPLETION_50, "Intermediate", "Complete habits 50 times", "🏆"));
        achievements.add(new Achievement(COMPLETION_100, "Expert", "Complete habits 100 times", "💎"));
        return achievements;
    }

    public static class Achievement {
        public String id;
        public String title;
        public String description;
        public String icon;

        public Achievement(String id, String title, String description, String icon) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.icon = icon;
        }
    }
}
