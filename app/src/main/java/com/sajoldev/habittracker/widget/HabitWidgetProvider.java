package com.sajoldev.habittracker.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.sajoldev.habittracker.MainActivity;
import com.sajoldev.habittracker.R;
import com.sajoldev.habittracker.data.HabitDao;
import com.sajoldev.habittracker.data.HabitDatabase;
import com.sajoldev.habittracker.data.HabitEntity;
import com.sajoldev.habittracker.utils.DateUtils;

import java.util.List;

public class HabitWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.habit_widget);

        // Set up click to open app
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        // Get today's habits
        HabitDao habitDao = HabitDatabase.getInstance(context).habitDao();
        List<HabitEntity> habits = habitDao.getAllHabitsSync();
        String today = DateUtils.getTodayString();

        int totalHabits = habits.size();
        int completedHabits = 0;
        StringBuilder habitList = new StringBuilder();

        for (int i = 0; i < habits.size() && i < 5; i++) {
            HabitEntity habit = habits.get(i);
            boolean isCompleted = habit.isCompletedOnDate(today);
            if (isCompleted) completedHabits++;

            habitList.append(isCompleted ? "✓ " : "○ ")
                    .append(habit.getName())
                    .append("\n");
        }

        if (habits.size() > 5) {
            habitList.append("... and ").append(habits.size() - 5).append(" more");
        }

        // Update widget views
        views.setTextViewText(R.id.tvWidgetTitle, "Today's Habits");
        views.setTextViewText(R.id.tvWidgetProgress, completedHabits + "/" + totalHabits);
        views.setTextViewText(R.id.tvWidgetHabits, habitList.toString());

        // Update progress bar
        int progress = totalHabits > 0 ? (completedHabits * 100 / totalHabits) : 0;
        views.setProgressBar(R.id.widgetProgressBar, 100, progress, false);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, HabitWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    public static void updateAllWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, HabitWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}
