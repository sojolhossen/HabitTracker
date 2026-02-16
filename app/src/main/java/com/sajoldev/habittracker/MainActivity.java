package com.sajoldev.habittracker;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DefaultItemAnimator;

import com.sajoldev.habittracker.adapter.DatePickerAdapter;
import com.sajoldev.habittracker.adapter.HabitAdapter;
import com.sajoldev.habittracker.data.HabitEntity;
import com.sajoldev.habittracker.utils.DateUtils;
import com.sajoldev.habittracker.viewmodel.HabitViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MainActivity - Main screen of the Habit Tracker app
 * 
 * FEATURES:
 * - Displays list of habits in RecyclerView
 * - Horizontal date picker at top to view different days
 * - FAB to add new habits
 * - Long press to edit/delete habits
 * - Check/uncheck habits with animation
 * - Shows streak information for each habit
 * 
 * MIDNIGHT RESET:
 * When app opens, checks if it's a new day and resets daily check status
 */
public class MainActivity extends AppCompatActivity implements 
        HabitAdapter.OnHabitClickListener,
        DatePickerAdapter.OnDateSelectedListener {

    private HabitViewModel habitViewModel;
    private HabitAdapter habitAdapter;
    private DatePickerAdapter datePickerAdapter;
    
    private RecyclerView habitRecyclerView;
    private RecyclerView dateRecyclerView;
    private FloatingActionButton fabAddHabit;
    private LinearLayout tvEmptyState;
    private TextView tvHeaderTitle;
    private View adPlaceholder; // AdMob placeholder

    // Available colors for habits
    private int[] habitColors = {
            Color.parseColor("#FF6B6B"), // Red
            Color.parseColor("#4ECDC4"), // Teal
            Color.parseColor("#45B7D1"), // Blue
            Color.parseColor("#96CEB4"), // Green
            Color.parseColor("#FFEAA7"), // Yellow
            Color.parseColor("#DDA0DD"), // Purple
            Color.parseColor("#F8B500"), // Orange
            Color.parseColor("#FF6B9D")  // Pink
    };

    // Available icons (using default Android icons for simplicity)
    private int[] habitIcons = {
            android.R.drawable.ic_menu_edit,      // Edit/Write
            android.R.drawable.ic_menu_compass,   // Navigation/Explore
            android.R.drawable.ic_menu_directions,// Directions
            android.R.drawable.ic_menu_agenda,    // List/Agenda
            android.R.drawable.ic_menu_camera,    // Camera/Photos
            android.R.drawable.ic_menu_call,      // Call/Communication
            android.R.drawable.ic_menu_manage,    // Settings/Manage
            android.R.drawable.ic_menu_mylocation // Location
    };

    private int selectedColor = habitColors[0];
    private int selectedIcon = habitIcons[0];
    private View selectedColorItem = null;
    private View selectedIconItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ViewModel
        habitViewModel = new ViewModelProvider(this).get(HabitViewModel.class);

        // Initialize views
        initViews();

        // Setup RecyclerViews
        setupHabitRecyclerView();
        setupDatePicker();

        // Setup FAB
        setupFab();

        // Observe data
        observeHabits();

        // Check for midnight reset on app open
        habitViewModel.checkAndResetDailyHabits();

        // Setup AdMob placeholder
        setupAdPlaceholder();
    }

    private void initViews() {
        habitRecyclerView = findViewById(R.id.habitRecyclerView);
        dateRecyclerView = findViewById(R.id.dateRecyclerView);
        fabAddHabit = findViewById(R.id.fabAddHabit);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        adPlaceholder = findViewById(R.id.adPlaceholder);
    }

    /**
     * Setup AdMob placeholder
     * This is where you'll integrate AdMob banner ad later
     * For now, it just shows a placeholder view
     */
    private void setupAdPlaceholder() {
        // AdMob integration placeholder
        // To add AdMob later:
        // 1. Add dependency: implementation 'com.google.android.gms:play-services-ads:22.0.0'
        // 2. Add AdView here and load ad
        // 3. Set visibility to VISIBLE
        
        adPlaceholder.setVisibility(View.GONE); // Hide for now
        
        // Example AdMob code (commented out):
        /*
        MobileAds.initialize(this);
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        */
    }

    private void setupHabitRecyclerView() {
        habitAdapter = new HabitAdapter(this);
        habitRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        habitRecyclerView.setAdapter(habitAdapter);
        habitRecyclerView.setHasFixedSize(true);
        
        // Add item animation
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(300);
        itemAnimator.setRemoveDuration(300);
        habitRecyclerView.setItemAnimator(itemAnimator);
    }

    private void setupDatePicker() {
        // Create date range: 30 days before today to 7 days after
        Date[] dates = DateUtils.getDateRange(30, 7);
        datePickerAdapter = new DatePickerAdapter(dates, this);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        dateRecyclerView.setLayoutManager(layoutManager);
        dateRecyclerView.setAdapter(datePickerAdapter);
        
        // Scroll to today (middle position)
        dateRecyclerView.scrollToPosition(dates.length / 2);
    }

    private void setupFab() {
        fabAddHabit.setOnClickListener(v -> {
            showAddHabitDialog();
        });
    }

    private void observeHabits() {
        habitViewModel.getAllHabits().observe(this, habits -> {
            habitAdapter.setHabits(habits);
            
            // Show/hide empty state
            if (habits == null || habits.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                habitRecyclerView.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                habitRecyclerView.setVisibility(View.VISIBLE);
            }
            
            // Update header with habit count
            tvHeaderTitle.setText("My Habits (" + habits.size() + ")");
        });
    }

    /**
     * Show dialog to add new habit
     */
    private void showAddHabitDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_habit);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        // Set dialog width to 90% of screen width
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        EditText etHabitName = dialog.findViewById(R.id.etHabitName);
        EditText etHabitGoal = dialog.findViewById(R.id.etHabitGoal);
        GridLayout colorGrid = dialog.findViewById(R.id.colorGrid);
        GridLayout iconGrid = dialog.findViewById(R.id.iconGrid);
        TextView btnSave = dialog.findViewById(R.id.btnSave);
        TextView btnCancel = dialog.findViewById(R.id.btnCancel);

        // Reset selections for new habit
        selectedColor = habitColors[0];
        selectedIcon = habitIcons[0];
        selectedColorItem = null;
        selectedIconItem = null;

        // Setup color picker
        setupColorPicker(colorGrid);

        // Setup icon picker
        setupIconPicker(iconGrid);

        // Save button
        btnSave.setOnClickListener(v -> {
            String name = etHabitName.getText().toString().trim();
            String goal = etHabitGoal.getText().toString().trim();

            if (name.isEmpty()) {
                etHabitName.setError("Please enter habit name");
                return;
            }

            if (goal.isEmpty()) {
                goal = "Once a day";
            }

            // Create new habit
            HabitEntity habit = new HabitEntity(name, goal, selectedColor, selectedIcon);
            habitViewModel.insert(habit);

            // Show success animation
            Animation successAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            fabAddHabit.startAnimation(successAnimation);

            Toast.makeText(this, "Habit added!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Show dialog to edit existing habit
     */
    private void showEditHabitDialog(HabitEntity habit) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_habit);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        // Set dialog width to 90% of screen width
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        EditText etHabitName = dialog.findViewById(R.id.etHabitName);
        EditText etHabitGoal = dialog.findViewById(R.id.etHabitGoal);
        GridLayout colorGrid = dialog.findViewById(R.id.colorGrid);
        GridLayout iconGrid = dialog.findViewById(R.id.iconGrid);
        TextView btnSave = dialog.findViewById(R.id.btnSave);
        TextView btnCancel = dialog.findViewById(R.id.btnCancel);
        TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);

        // Set existing values
        tvDialogTitle.setText("Edit Habit");
        etHabitName.setText(habit.getName());
        etHabitGoal.setText(habit.getGoal());
        selectedColor = habit.getColor();
        selectedIcon = habit.getIconResourceId();

        // Setup color picker with current color selected
        setupColorPicker(colorGrid);

        // Setup icon picker with current icon selected
        setupIconPicker(iconGrid);

        // Save button
        btnSave.setOnClickListener(v -> {
            String name = etHabitName.getText().toString().trim();
            String goal = etHabitGoal.getText().toString().trim();

            if (name.isEmpty()) {
                etHabitName.setError("Please enter habit name");
                return;
            }

            // Update habit
            habit.setName(name);
            habit.setGoal(goal.isEmpty() ? "Once a day" : goal);
            habit.setColor(selectedColor);
            habit.setIconResourceId(selectedIcon);
            habitViewModel.update(habit);

            Toast.makeText(this, "Habit updated!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Show delete confirmation dialog
     */
    private void showDeleteDialog(HabitEntity habit) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Habit")
                .setMessage("Are you sure you want to delete \"" + habit.getName() + "\"? This will also delete all streak history.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    habitViewModel.delete(habit);
                    Toast.makeText(this, "Habit deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupColorPicker(GridLayout colorGrid) {
        colorGrid.removeAllViews();

        for (int i = 0; i < habitColors.length; i++) {
            View colorItem = LayoutInflater.from(this).inflate(R.layout.item_color_picker, colorGrid, false);
            View colorCircle = colorItem.findViewById(R.id.colorCircle);
            View selectionRing = colorItem.findViewById(R.id.selectionRing);

            // Set the actual color
            colorCircle.setBackgroundColor(habitColors[i]);

            // Mark selected
            if (habitColors[i] == selectedColor) {
                selectionRing.setVisibility(View.VISIBLE);
                selectedColorItem = colorItem;
            } else {
                selectionRing.setVisibility(View.INVISIBLE);
            }

            final int color = habitColors[i];
            colorItem.setOnClickListener(v -> {
                // Deselect previous
                if (selectedColorItem != null) {
                    View prevRing = selectedColorItem.findViewById(R.id.selectionRing);
                    if (prevRing != null) {
                        prevRing.setVisibility(View.INVISIBLE);
                    }
                }

                // Select new
                selectedColor = color;
                selectedColorItem = colorItem;
                selectionRing.setVisibility(View.VISIBLE);
            });

            colorGrid.addView(colorItem);
        }
    }

    private void setupIconPicker(GridLayout iconGrid) {
        iconGrid.removeAllViews();

        for (int i = 0; i < habitIcons.length; i++) {
            View iconItem = LayoutInflater.from(this).inflate(R.layout.item_icon_picker, iconGrid, false);
            ImageView iconImage = iconItem.findViewById(R.id.iconImage);
            View selectionBg = iconItem.findViewById(R.id.selectionBg);

            iconImage.setImageResource(habitIcons[i]);
            iconImage.setColorFilter(getResources().getColor(R.color.colorTextPrimary));

            // Mark selected
            if (habitIcons[i] == selectedIcon) {
                selectionBg.setVisibility(View.VISIBLE);
                iconImage.setColorFilter(getResources().getColor(android.R.color.white));
                selectedIconItem = iconItem;
            } else {
                selectionBg.setVisibility(View.INVISIBLE);
            }

            final int icon = habitIcons[i];
            iconItem.setOnClickListener(v -> {
                // Deselect previous
                if (selectedIconItem != null) {
                    View prevBg = selectedIconItem.findViewById(R.id.selectionBg);
                    ImageView prevIcon = selectedIconItem.findViewById(R.id.iconImage);
                    if (prevBg != null) {
                        prevBg.setVisibility(View.INVISIBLE);
                    }
                    if (prevIcon != null) {
                        prevIcon.setColorFilter(getResources().getColor(R.color.colorTextPrimary));
                    }
                }

                // Select new
                selectedIcon = icon;
                selectedIconItem = iconItem;
                selectionBg.setVisibility(View.VISIBLE);
                iconImage.setColorFilter(getResources().getColor(android.R.color.white));
            });

            iconGrid.addView(iconItem);
        }
    }

    // HabitAdapter.OnHabitClickListener implementations

    @Override
    public void onHabitClick(HabitEntity habit, int position) {
        // Optional: Show habit details
        // showHabitDetailsDialog(habit);
    }

    @Override
    public void onHabitLongClick(HabitEntity habit, int position) {
        // Show options dialog
        String[] options = {"Edit", "Delete"};
        new AlertDialog.Builder(this)
                .setTitle(habit.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditHabitDialog(habit);
                    } else if (which == 1) {
                        showDeleteDialog(habit);
                    }
                })
                .show();
    }

    @Override
    public void onCheckClick(HabitEntity habit, int position, boolean isChecked) {
        String todayStr = DateUtils.getTodayString();
        habitViewModel.toggleHabitCompletion(habit, todayStr, isChecked);
        
        if (isChecked) {
            Toast.makeText(this, "Great job! Keep it up! 🔥", Toast.LENGTH_SHORT).show();
        }
    }

    // DatePickerAdapter.OnDateSelectedListener implementation

    @Override
    public void onDateSelected(Date date, int position) {
        // Update habit list to show completion status for selected date
        habitAdapter.setSelectedDate(date);
        
        // Update header
        if (DateUtils.isToday(date)) {
            tvHeaderTitle.setText("Today's Habits");
        } else {
            tvHeaderTitle.setText(DateUtils.getDisplayDate(date));
        }
    }
}
