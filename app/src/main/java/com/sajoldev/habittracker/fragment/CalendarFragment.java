package com.sajoldev.habittracker.fragment;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sajoldev.habittracker.R;
import com.sajoldev.habittracker.adapter.HabitAdapter;
import com.sajoldev.habittracker.data.HabitEntity;
import com.sajoldev.habittracker.utils.DateUtils;
import com.sajoldev.habittracker.viewmodel.HabitViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarFragment extends Fragment {

    private static final String TAG = "CalendarFragment";
    
    private HabitViewModel viewModel;
    private List<HabitEntity> allHabits = new ArrayList<>();

    private TextView tvMonthYear;
    private GridLayout calendarGrid;
    private RecyclerView rvHabitsForDate;
    private LinearLayout emptyStateContainer;
    private HabitAdapter habitAdapter;
    private TextView tvSelectedDate;

    private Calendar currentCalendar;
    private Date selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);

        initViews(view);
        currentCalendar = Calendar.getInstance();
        selectedDate = new Date();

        setupHabitsList();

        viewModel.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            this.allHabits = habits != null ? habits : new ArrayList<>();
            updateCalendar();
            updateHabitsForDate(selectedDate);
        });

        return view;
    }

    private void initViews(View view) {
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        calendarGrid = view.findViewById(R.id.calendarGrid);
        rvHabitsForDate = view.findViewById(R.id.rvHabitsForDate);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);

        ImageButton btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        ImageButton btnNextMonth = view.findViewById(R.id.btnNextMonth);

        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });
    }

    private void setupHabitsList() {
        habitAdapter = new HabitAdapter(new HabitAdapter.OnHabitClickListener() {
            @Override
            public void onHabitClick(HabitEntity habit, int position) {}

            @Override
            public void onHabitLongClick(HabitEntity habit, int position) {}

            @Override
            public void onCheckClick(HabitEntity habit, int position, boolean isChecked) {
                String dateStr = DateUtils.dateToString(selectedDate);
                viewModel.toggleHabitCompletion(habit, dateStr, isChecked);
                updateCalendar();
                updateHabitsForDate(selectedDate);
            }
        });

        rvHabitsForDate.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHabitsForDate.setAdapter(habitAdapter);
    }

    private void updateCalendar() {
        if (calendarGrid == null) return;
        
        calendarGrid.removeAllViews();
        calendarGrid.setColumnCount(7);

        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        String monthYear = android.text.format.DateFormat.format("MMMM yyyy", cal).toString();
        tvMonthYear.setText(monthYear);
        
        // Get first day of month (1 = Sunday, 2 = Monday, ..., 7 = Saturday)
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        Calendar today = Calendar.getInstance();

        // Add empty cells for days before the first day of month
        int emptyCells = firstDayOfWeek - 1;
        
        for (int i = 0; i < emptyCells; i++) {
            View emptyView = createEmptyCell();
            calendarGrid.addView(emptyView);
        }

        // Add day cells
        for (int day = 1; day <= daysInMonth; day++) {
            cal.set(Calendar.DAY_OF_MONTH, day);
            Date date = cal.getTime();
            String dateStr = DateUtils.dateToString(date);

            int totalHabits = allHabits.size();
            int completedHabits = 0;
            for (HabitEntity habit : allHabits) {
                if (habit.isCompletedOnDate(dateStr)) {
                    completedHabits++;
                }
            }

            boolean isToday = DateUtils.isSameDay(date, today.getTime());
            boolean isSelected = DateUtils.isSameDay(date, selectedDate);

            View dayView = createDayCell(day, completedHabits, totalHabits, isToday, isSelected);
            dayView.setOnClickListener(v -> {
                selectedDate = date;
                updateCalendar();
                updateHabitsForDate(date);
            });

            calendarGrid.addView(dayView);
        }
    }

    private View createEmptyCell() {
        LinearLayout emptyView = new LinearLayout(getContext());
        emptyView.setOrientation(LinearLayout.VERTICAL);
        emptyView.setGravity(android.view.Gravity.CENTER);
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(2, 2, 2, 2);
        emptyView.setLayoutParams(params);
        
        // Add invisible text to maintain consistent height
        TextView placeholder = new TextView(getContext());
        placeholder.setText(" ");
        placeholder.setTextSize(16);
        placeholder.setPadding(8, 12, 8, 12);
        emptyView.addView(placeholder);
        
        return emptyView;
    }

    private View createDayCell(int day, int completed, int total, boolean isToday, boolean isSelected) {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(android.view.Gravity.CENTER);
        container.setPadding(4, 8, 4, 8);
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(2, 2, 2, 2);
        container.setLayoutParams(params);

        // Create circular background
        int size = 40;
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        background.setSize(size, size);
        
        if (isSelected) {
            background.setColor(Color.parseColor("#2196F3")); // Blue for selected
        } else if (isToday) {
            background.setColor(Color.TRANSPARENT);
            background.setStroke(3, Color.parseColor("#FF6D00")); // Orange border for today
        } else {
            background.setColor(Color.TRANSPARENT);
        }
        
        container.setBackground(background);

        // Day number
        TextView dayText = new TextView(getContext());
        dayText.setText(String.valueOf(day));
        dayText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        dayText.setTextSize(16);
        dayText.setPadding(8, 12, 8, 12);
        
        if (isSelected) {
            dayText.setTextColor(Color.WHITE);
            dayText.setTypeface(null, android.graphics.Typeface.BOLD);
        } else if (isToday) {
            dayText.setTextColor(Color.parseColor("#FF6D00"));
            dayText.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            dayText.setTextColor(Color.parseColor("#212121"));
        }
        
        container.addView(dayText);

        // Progress indicator dot
        if (total > 0) {
            View dot = new View(getContext());
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(6, 6);
            dot.setLayoutParams(dotParams);
            dot.setBackgroundResource(R.drawable.circle_shape);
            
            if (completed == total && completed > 0) {
                dot.setBackgroundColor(Color.parseColor("#4CAF50")); // All completed - green
            } else if (completed > 0) {
                dot.setBackgroundColor(Color.parseColor("#FF9800")); // Partial - orange
            } else {
                dot.setBackgroundColor(Color.TRANSPARENT); // None - invisible
            }
            
            container.addView(dot);
        }

        return container;
    }

    private void updateHabitsForDate(Date date) {
        String dateStr = DateUtils.dateToString(date);
        tvSelectedDate.setText(android.text.format.DateFormat.format("EEEE, MMMM dd, yyyy", date));

        List<HabitEntity> habitsForDate = new ArrayList<>();
        for (HabitEntity habit : allHabits) {
            if (shouldShowHabitOnDate(habit, date)) {
                habitsForDate.add(habit);
            }
        }

        Log.d(TAG, "Updating habits for date: " + dateStr + ", found: " + habitsForDate.size() + " habits");

        habitAdapter.setHabits(habitsForDate);
        habitAdapter.setSelectedDate(date);

        // Show/hide empty state
        if (habitsForDate.isEmpty()) {
            rvHabitsForDate.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            rvHabitsForDate.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        }
    }

    private boolean shouldShowHabitOnDate(HabitEntity habit, Date date) {
        // Check if date is before habit creation date
        Date createdDate = habit.getCreatedDate();
        if (createdDate != null) {
            // Normalize dates to remove time component
            Calendar createdCal = Calendar.getInstance();
            createdCal.setTime(createdDate);
            createdCal.set(Calendar.HOUR_OF_DAY, 0);
            createdCal.set(Calendar.MINUTE, 0);
            createdCal.set(Calendar.SECOND, 0);
            createdCal.set(Calendar.MILLISECOND, 0);
            
            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(date);
            dateCal.set(Calendar.HOUR_OF_DAY, 0);
            dateCal.set(Calendar.MINUTE, 0);
            dateCal.set(Calendar.SECOND, 0);
            dateCal.set(Calendar.MILLISECOND, 0);
            
            // If the date is before the creation date, don't show the habit
            if (dateCal.before(createdCal)) {
                return false;
            }
        }
        
        String frequency = habit.getFrequency();
        
        // Default to Daily if no frequency is set
        if (frequency == null || frequency.isEmpty() || "Daily".equals(frequency)) {
            return true;
        } else if ("Weekly".equals(frequency)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            // Show on the day the habit was created or every Monday
            return dayOfWeek == Calendar.MONDAY;
        } else if ("Custom".equals(frequency)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            String[] daysMap = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            String dayName = daysMap[dayOfWeek - 1];
            String selectedDays = habit.getSelectedDays();
            // If no days selected, show every day
            if (selectedDays == null || selectedDays.isEmpty()) {
                return true;
            }
            return selectedDays.contains(dayName);
        }

        return true;
    }
}
