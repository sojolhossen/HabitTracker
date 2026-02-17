package com.sajoldev.habittracker.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.app.TimePickerDialog;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DefaultItemAnimator;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sajoldev.habittracker.R;
import com.sajoldev.habittracker.adapter.DatePickerAdapter;
import com.sajoldev.habittracker.adapter.HabitAdapter;
import com.sajoldev.habittracker.data.HabitEntity;
import com.sajoldev.habittracker.notification.ReminderReceiver;
import com.sajoldev.habittracker.utils.AchievementManager;
import com.sajoldev.habittracker.utils.DateUtils;
import com.sajoldev.habittracker.viewmodel.HabitViewModel;
import com.sajoldev.habittracker.widget.HabitWidgetProvider;

import java.util.Date;

public class HomeFragment extends Fragment implements HabitAdapter.OnHabitClickListener, DatePickerAdapter.OnDateSelectedListener {

    private HabitViewModel habitViewModel;
    private HabitAdapter habitAdapter;
    private DatePickerAdapter datePickerAdapter;

    private RecyclerView habitRecyclerView;
    private RecyclerView dateRecyclerView;
    private FloatingActionButton fabAddHabit;
    private LinearLayout tvEmptyState;
    private TextView tvHeaderTitle;

    private int[] habitColors = {
            Color.parseColor("#FF6B6B"),
            Color.parseColor("#4ECDC4"),
            Color.parseColor("#45B7D1"),
            Color.parseColor("#96CEB4"),
            Color.parseColor("#FFEAA7"),
            Color.parseColor("#DDA0DD"),
            Color.parseColor("#F8B500"),
            Color.parseColor("#FF6B9D")
    };

    private int[] habitIcons = {
            android.R.drawable.ic_menu_edit,
            android.R.drawable.ic_menu_compass,
            android.R.drawable.ic_menu_directions,
            android.R.drawable.ic_menu_agenda,
            android.R.drawable.ic_menu_camera,
            android.R.drawable.ic_menu_call,
            android.R.drawable.ic_menu_manage,
            android.R.drawable.ic_menu_mylocation
    };

    private int selectedColor = habitColors[0];
    private int selectedIcon = habitIcons[0];
    private View selectedColorItem = null;
    private View selectedIconItem = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        habitViewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);

        initViews(view);
        setupHabitRecyclerView();
        setupDatePicker();
        setupFab();
        observeHabits();

        habitViewModel.checkAndResetDailyHabits();

        return view;
    }

    private void initViews(View view) {
        habitRecyclerView = view.findViewById(R.id.habitRecyclerView);
        dateRecyclerView = view.findViewById(R.id.dateRecyclerView);
        fabAddHabit = view.findViewById(R.id.fabAddHabit);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle);
    }

    private void setupHabitRecyclerView() {
        habitAdapter = new HabitAdapter(this);
        habitRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        habitRecyclerView.setAdapter(habitAdapter);

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(300);
        itemAnimator.setRemoveDuration(300);
        habitRecyclerView.setItemAnimator(itemAnimator);
    }

    private void setupDatePicker() {
        Date[] dates = DateUtils.getDateRange(30, 7);
        datePickerAdapter = new DatePickerAdapter(dates, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        dateRecyclerView.setLayoutManager(layoutManager);
        dateRecyclerView.setAdapter(datePickerAdapter);

        // Scroll to today's position (30 days before + today = index 30)
        int todayPosition = 30; // Since we have 30 days before today
        dateRecyclerView.scrollToPosition(todayPosition);
        
        // Notify the adapter that today is selected
        datePickerAdapter.setSelectedPosition(todayPosition);
    }

    private void setupFab() {
        fabAddHabit.setOnClickListener(v -> showAddHabitDialog());
    }

    private void observeHabits() {
        habitViewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            habitAdapter.setHabits(habits);

            if (habits == null || habits.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                habitRecyclerView.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                habitRecyclerView.setVisibility(View.VISIBLE);
            }

            tvHeaderTitle.setText("My Habits (" + habits.size() + ")");

            AchievementManager achievementManager = new AchievementManager(requireContext());
            achievementManager.checkAchievements(requireContext(), habits);

            HabitWidgetProvider.updateAllWidgets(requireContext());
        });
    }

    public void showAddHabitDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_habit);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

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

        Spinner spinnerCategory = dialog.findViewById(R.id.spinnerCategory);
        RadioGroup radioGroupFrequency = dialog.findViewById(R.id.radioGroupFrequency);
        GridLayout daysGrid = dialog.findViewById(R.id.daysGrid);
        Switch switchReminder = dialog.findViewById(R.id.switchReminder);
        TextView tvReminderTime = dialog.findViewById(R.id.tvReminderTime);

        String[] categories = {"Health", "Productivity", "Fitness", "Learning", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        final int[] reminderHour = {9};
        final int[] reminderMinute = {0};

        tvReminderTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
                reminderHour[0] = hourOfDay;
                reminderMinute[0] = minute;
                tvReminderTime.setText(String.format("%02d:%02d", hourOfDay, minute));
            }, reminderHour[0], reminderMinute[0], true);
            timePickerDialog.show();
        });

        radioGroupFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioCustom) {
                daysGrid.setVisibility(View.VISIBLE);
            } else {
                daysGrid.setVisibility(View.GONE);
            }
        });

        selectedColor = habitColors[0];
        selectedIcon = habitIcons[0];
        selectedColorItem = null;
        selectedIconItem = null;

        setupColorPicker(colorGrid);
        setupIconPicker(iconGrid);

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

            HabitEntity habit = new HabitEntity(name, goal, selectedColor, selectedIcon);
            habit.setCategory(spinnerCategory.getSelectedItem().toString());

            int selectedFrequencyId = radioGroupFrequency.getCheckedRadioButtonId();
            if (selectedFrequencyId == R.id.radioDaily) {
                habit.setFrequency("Daily");
            } else if (selectedFrequencyId == R.id.radioWeekly) {
                habit.setFrequency("Weekly");
            } else {
                habit.setFrequency("Custom");
                StringBuilder days = new StringBuilder();
                CheckBox cbMon = dialog.findViewById(R.id.cbMon);
                CheckBox cbTue = dialog.findViewById(R.id.cbTue);
                CheckBox cbWed = dialog.findViewById(R.id.cbWed);
                CheckBox cbThu = dialog.findViewById(R.id.cbThu);
                CheckBox cbFri = dialog.findViewById(R.id.cbFri);
                CheckBox cbSat = dialog.findViewById(R.id.cbSat);
                CheckBox cbSun = dialog.findViewById(R.id.cbSun);
                if (cbMon.isChecked()) days.append("Mon,");
                if (cbTue.isChecked()) days.append("Tue,");
                if (cbWed.isChecked()) days.append("Wed,");
                if (cbThu.isChecked()) days.append("Thu,");
                if (cbFri.isChecked()) days.append("Fri,");
                if (cbSat.isChecked()) days.append("Sat,");
                if (cbSun.isChecked()) days.append("Sun,");
                habit.setSelectedDays(days.toString());
            }

            habit.setReminderEnabled(switchReminder.isChecked());
            habit.setReminderHour(reminderHour[0]);
            habit.setReminderMinute(reminderMinute[0]);

            habitViewModel.insert(habit);

            if (switchReminder.isChecked()) {
                ReminderReceiver.scheduleReminder(requireContext(), habit.getId(), name, goal, reminderHour[0], reminderMinute[0]);
            }

            Toast.makeText(getContext(), "Habit added!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void setupColorPicker(GridLayout colorGrid) {
        colorGrid.removeAllViews();

        for (int i = 0; i < habitColors.length; i++) {
            View colorItem = LayoutInflater.from(getContext()).inflate(R.layout.item_color_picker, colorGrid, false);
            View colorCircle = colorItem.findViewById(R.id.colorCircle);
            View selectionRing = colorItem.findViewById(R.id.selectionRing);

            colorCircle.setBackgroundColor(habitColors[i]);

            if (habitColors[i] == selectedColor) {
                selectionRing.setVisibility(View.VISIBLE);
                selectedColorItem = colorItem;
            } else {
                selectionRing.setVisibility(View.INVISIBLE);
            }

            final int color = habitColors[i];
            colorItem.setOnClickListener(v -> {
                if (selectedColorItem != null) {
                    View prevRing = selectedColorItem.findViewById(R.id.selectionRing);
                    if (prevRing != null) {
                        prevRing.setVisibility(View.INVISIBLE);
                    }
                }

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
            View iconItem = LayoutInflater.from(getContext()).inflate(R.layout.item_icon_picker, iconGrid, false);
            ImageView iconImage = iconItem.findViewById(R.id.iconImage);
            View selectionBg = iconItem.findViewById(R.id.selectionBg);

            iconImage.setImageResource(habitIcons[i]);
            iconImage.setColorFilter(getResources().getColor(R.color.colorTextPrimary));

            if (habitIcons[i] == selectedIcon) {
                selectionBg.setVisibility(View.VISIBLE);
                iconImage.setColorFilter(getResources().getColor(android.R.color.white));
                selectedIconItem = iconItem;
            } else {
                selectionBg.setVisibility(View.INVISIBLE);
            }

            final int icon = habitIcons[i];
            iconItem.setOnClickListener(v -> {
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

                selectedIcon = icon;
                selectedIconItem = iconItem;
                selectionBg.setVisibility(View.VISIBLE);
                iconImage.setColorFilter(getResources().getColor(android.R.color.white));
            });

            iconGrid.addView(iconItem);
        }
    }

    @Override
    public void onHabitClick(HabitEntity habit, int position) {
        // Optional: Show habit details
    }

    @Override
    public void onHabitLongClick(HabitEntity habit, int position) {
        // Show options dialog
        String[] options = {"Edit", "Delete"};
        new android.app.AlertDialog.Builder(getContext())
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
            Toast.makeText(getContext(), "Great job! Keep it up! 🔥", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDateSelected(Date date, int position) {
        habitAdapter.setSelectedDate(date);

        if (DateUtils.isToday(date)) {
            tvHeaderTitle.setText("Today's Habits");
        } else {
            tvHeaderTitle.setText(DateUtils.getDisplayDate(date));
        }
    }

    private void showEditHabitDialog(HabitEntity habit) {
        // Similar to showAddHabitDialog but with pre-filled values
        Toast.makeText(getContext(), "Edit feature - to be implemented", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteDialog(HabitEntity habit) {
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Habit")
                .setMessage("Are you sure you want to delete \"" + habit.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    habitViewModel.delete(habit);
                    Toast.makeText(getContext(), "Habit deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
