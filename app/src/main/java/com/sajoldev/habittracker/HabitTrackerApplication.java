package com.sajoldev.habittracker;

import android.app.Application;

/**
 * HabitTrackerApplication - Application class
 * Initialize app-wide configurations here
 */
public class HabitTrackerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize any app-wide components here
        // Example: Crash reporting, analytics, etc.
        
        // Initialize AdMob when ready:
        // MobileAds.initialize(this, "YOUR_ADMOB_APP_ID");
    }
}
