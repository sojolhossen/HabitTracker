package com.sajoldev.habittracker.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * HabitEntity - Room Database Entity
 * Stores all habit information including completion history for streak calculation
 */
@Entity(tableName = "habits")
@TypeConverters({DateConverter.class, StringSetConverter.class})
public class HabitEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String goal;
    private int color;
    private int iconResourceId;
    private String category;
    private Date createdDate;

    // Frequency settings (Daily, Weekly, Custom)
    private String frequency; // "Daily", "Weekly", "Custom"
    private String selectedDays; // "Mon,Wed,Fri" for custom days

    // Reminder settings
    private boolean reminderEnabled;
    private int reminderHour;
    private int reminderMinute;

    // Streak tracking
    private int currentStreak;
    private int longestStreak;

    // Set of dates (in "yyyy-MM-dd" format) when habit was completed
    private Set<String> completedDates;

    // Track when the habit was last checked (for midnight reset logic)
    private Date lastCheckedDate;

    // Whether habit is checked for the current viewing date
    private boolean isCheckedToday;

    public HabitEntity(String name, String goal, int color, int iconResourceId) {
        this.name = name;
        this.goal = goal;
        this.color = color;
        this.iconResourceId = iconResourceId;
        this.category = "Other";
        this.createdDate = new Date();
        this.frequency = "Daily";
        this.selectedDays = "";
        this.reminderEnabled = true;
        this.reminderHour = 9;
        this.reminderMinute = 0;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.completedDates = new HashSet<>();
        this.lastCheckedDate = new Date();
        this.isCheckedToday = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public int getIconResourceId() { return iconResourceId; }
    public void setIconResourceId(int iconResourceId) { this.iconResourceId = iconResourceId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getSelectedDays() { return selectedDays; }
    public void setSelectedDays(String selectedDays) { this.selectedDays = selectedDays; }

    public boolean isReminderEnabled() { return reminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { this.reminderEnabled = reminderEnabled; }

    public int getReminderHour() { return reminderHour; }
    public void setReminderHour(int reminderHour) { this.reminderHour = reminderHour; }

    public int getReminderMinute() { return reminderMinute; }
    public void setReminderMinute(int reminderMinute) { this.reminderMinute = reminderMinute; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }

    public Set<String> getCompletedDates() { return completedDates; }
    public void setCompletedDates(Set<String> completedDates) { this.completedDates = completedDates; }

    public Date getLastCheckedDate() { return lastCheckedDate; }
    public void setLastCheckedDate(Date lastCheckedDate) { this.lastCheckedDate = lastCheckedDate; }

    public boolean isCheckedToday() { return isCheckedToday; }
    public void setCheckedToday(boolean checkedToday) { isCheckedToday = checkedToday; }

    /**
     * Check if habit was completed on a specific date
     * @param dateStr Date in "yyyy-MM-dd" format
     * @return true if completed on that date
     */
    public boolean isCompletedOnDate(String dateStr) {
        return completedDates != null && completedDates.contains(dateStr);
    }

    /**
     * Mark habit as completed on a specific date
     * @param dateStr Date in "yyyy-MM-dd" format
     */
    public void markCompletedOnDate(String dateStr) {
        if (completedDates == null) {
            completedDates = new HashSet<>();
        }
        completedDates.add(dateStr);
    }

    /**
     * Unmark habit completion on a specific date
     * @param dateStr Date in "yyyy-MM-dd" format
     */
    public void unmarkCompletedOnDate(String dateStr) {
        if (completedDates != null) {
            completedDates.remove(dateStr);
        }
    }

    /**
     * Get completion dates as a List
     * @return List of completion date strings
     */
    public java.util.List<String> getCompletionDatesList() {
        return completedDates != null ? new java.util.ArrayList<>(completedDates) : new java.util.ArrayList<>();
    }
}
