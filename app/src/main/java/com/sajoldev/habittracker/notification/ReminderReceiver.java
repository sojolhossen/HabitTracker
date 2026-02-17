package com.sajoldev.habittracker.notification;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.sajoldev.habittracker.MainActivity;
import com.sajoldev.habittracker.R;
import com.sajoldev.habittracker.data.HabitDao;
import com.sajoldev.habittracker.data.HabitDatabase;
import com.sajoldev.habittracker.data.HabitEntity;
import com.sajoldev.habittracker.utils.DateUtils;

import java.util.Calendar;
import java.util.List;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "habit_reminder_channel";
    private static final String CHANNEL_NAME = "Habit Reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        int habitId = intent.getIntExtra("habit_id", -1);
        String habitName = intent.getStringExtra("habit_name");
        String habitGoal = intent.getStringExtra("habit_goal");

        if (habitId != -1 && habitName != null) {
            // Check if already completed today
            HabitDao habitDao = HabitDatabase.getInstance(context).habitDao();
            HabitEntity habit = habitDao.getHabitById(habitId);
            
            if (habit != null && !habit.isCompletedOnDate(DateUtils.getTodayString())) {
                showNotification(context, habitId, habitName, habitGoal);
            }
        }
    }

    private void showNotification(Context context, int habitId, String habitName, String habitGoal) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("habit_id", habitId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            habitId, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_check_circle)
            .setContentTitle("Time for: " + habitName)
            .setContentText(habitGoal != null ? habitGoal : "Don't forget to complete your habit!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(new long[]{0, 500, 200, 500})
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_check_circle, "Mark Done", createActionIntent(context, habitId));

        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(habitId, builder.build());
    }

    private PendingIntent createActionIntent(Context context, int habitId) {
        Intent actionIntent = new Intent(context, ReminderActionReceiver.class);
        actionIntent.putExtra("habit_id", habitId);
        actionIntent.setAction("MARK_COMPLETE");
        
        return PendingIntent.getBroadcast(
            context,
            habitId,
            actionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Reminders for your daily habits");
            channel.enableVibration(true);
            
            NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void scheduleReminder(Context context, int habitId, String habitName, 
                                       String habitGoal, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("habit_id", habitId);
        intent.putExtra("habit_name", habitName);
        intent.putExtra("habit_goal", habitGoal);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // If time has passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.getTimeInMillis(),
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        );
    }

    public static void cancelReminder(Context context, int habitId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }
}
