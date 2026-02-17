package com.sajoldev.habittracker.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sajoldev.habittracker.data.HabitDao;
import com.sajoldev.habittracker.data.HabitDatabase;
import com.sajoldev.habittracker.data.HabitEntity;
import com.sajoldev.habittracker.utils.DateUtils;

public class ReminderActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("MARK_COMPLETE".equals(intent.getAction())) {
            int habitId = intent.getIntExtra("habit_id", -1);
            
            if (habitId != -1) {
                HabitDao habitDao = HabitDatabase.getInstance(context).habitDao();
                HabitEntity habit = habitDao.getHabitById(habitId);
                
                if (habit != null) {
                    String today = DateUtils.getTodayString();
                    if (!habit.isCompletedOnDate(today)) {
                        habit.markCompletedOnDate(today);
                        habit.setCurrentStreak(habit.getCurrentStreak() + 1);
                        habit.setCheckedToday(true);
                        habitDao.updateHabit(habit);
                    }
                }
                
                // Cancel the notification
                android.app.NotificationManager notificationManager = 
                    (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(habitId);
            }
        }
    }
}
