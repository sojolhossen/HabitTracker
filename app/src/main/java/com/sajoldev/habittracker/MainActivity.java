package com.sajoldev.habittracker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sajoldev.habittracker.fragment.AchievementsFragment;
import com.sajoldev.habittracker.fragment.CalendarFragment;
import com.sajoldev.habittracker.fragment.HomeFragment;
import com.sajoldev.habittracker.fragment.SettingsFragment;
import com.sajoldev.habittracker.fragment.StatisticsFragment;
import com.sajoldev.habittracker.viewmodel.HabitViewModel;

public class MainActivity extends AppCompatActivity {

    private HabitViewModel habitViewModel;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        habitViewModel = new ViewModelProvider(this).get(HabitViewModel.class);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        setupBottomNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

        // Check for midnight reset on app open
        habitViewModel.checkAndResetDailyHabits();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_calendar) {
                selectedFragment = new CalendarFragment();
            } else if (itemId == R.id.nav_statistics) {
                selectedFragment = new StatisticsFragment();
            } else if (itemId == R.id.nav_achievements) {
                selectedFragment = new AchievementsFragment();
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
