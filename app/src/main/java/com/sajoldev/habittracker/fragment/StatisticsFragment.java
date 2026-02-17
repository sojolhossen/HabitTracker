package com.sajoldev.habittracker.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.sajoldev.habittracker.R;
import com.sajoldev.habittracker.data.HabitEntity;
import com.sajoldev.habittracker.utils.CountAnimation;
import com.sajoldev.habittracker.utils.DateUtils;
import com.sajoldev.habittracker.viewmodel.HabitViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private HabitViewModel viewModel;
    private List<HabitEntity> habits = new ArrayList<>();
    private HabitEntity selectedHabit;

    private LineChart lineChart;
    private BarChart barChart;
    private PieChart pieChart;

    private TextView tvTotalHabits;
    private TextView tvCompletedToday;
    private TextView tvBestStreak;
    private TextView tvSuccessRate;
    private TextView tvWeeklyAverage;

    private Spinner habitSpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);

        initViews(view);
        initCharts();

        viewModel.getAllHabits().observe(getViewLifecycleOwner(), habitList -> {
            this.habits = habitList;
            setupHabitSpinner();
            updateOverallStats();
            updateCharts();
        });

        return view;
    }

    private void initViews(View view) {
        lineChart = view.findViewById(R.id.lineChart);
        barChart = view.findViewById(R.id.barChart);
        pieChart = view.findViewById(R.id.pieChart);

        tvTotalHabits = view.findViewById(R.id.tvTotalHabits);
        tvCompletedToday = view.findViewById(R.id.tvCompletedToday);
        tvBestStreak = view.findViewById(R.id.tvBestStreak);
        tvSuccessRate = view.findViewById(R.id.tvSuccessRate);
        tvWeeklyAverage = view.findViewById(R.id.tvWeeklyAverage);

        habitSpinner = view.findViewById(R.id.habitSpinner);
    }

    private void initCharts() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.animateX(2000);

        barChart.getDescription().setEnabled(false);
        barChart.setTouchEnabled(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getAxisRight().setEnabled(false);
        barChart.animateY(2000);

        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.animateY(2000);
    }

    private void setupHabitSpinner() {
        List<String> habitNames = new ArrayList<>();
        habitNames.add("All Habits");
        for (HabitEntity habit : habits) {
            habitNames.add(habit.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, habitNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        habitSpinner.setAdapter(adapter);

        habitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedHabit = null;
                } else {
                    selectedHabit = habits.get(position - 1);
                }
                updateCharts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateOverallStats() {
        CountAnimation.animateCount(tvTotalHabits, 0, habits.size(), 1500);

        int completedToday = 0;
        String today = DateUtils.getTodayString();
        for (HabitEntity habit : habits) {
            if (habit.isCompletedOnDate(today)) {
                completedToday++;
            }
        }
        CountAnimation.animateCountFraction(tvCompletedToday, 0, completedToday, habits.size(), 1500);

        int bestStreak = 0;
        for (HabitEntity habit : habits) {
            if (habit.getCurrentStreak() > bestStreak) {
                bestStreak = habit.getCurrentStreak();
            }
        }
        CountAnimation.animateCountWithSuffix(tvBestStreak, 0, bestStreak, " days", 1500);

        int totalCompletions = 0;
        int totalPossible = 0;
        for (HabitEntity habit : habits) {
            List<String> completionDates = habit.getCompletionDatesList();
            totalCompletions += completionDates.size();
            long daysSinceCreated = DateUtils.getDaysBetween(habit.getCreatedDate(), new Date());
            totalPossible += Math.max(1, daysSinceCreated);
        }
        int successRate = totalPossible > 0 ? (totalCompletions * 100 / totalPossible) : 0;
        CountAnimation.animateCountWithSuffix(tvSuccessRate, 0, successRate, "%", 1500);

        int weeklyTotal = 0;
        for (HabitEntity habit : habits) {
            weeklyTotal += getWeeklyCompletions(habit);
        }
        int weeklyAverage = habits.size() > 0 ? weeklyTotal / habits.size() : 0;
        CountAnimation.animateCountFraction(tvWeeklyAverage, 0, weeklyAverage, 7, 1500);
    }

    private int getWeeklyCompletions(HabitEntity habit) {
        int count = 0;
        Calendar cal = Calendar.getInstance();
        List<String> completionDates = habit.getCompletionDatesList();

        for (int i = 0; i < 7; i++) {
            String dateStr = DateUtils.dateToString(cal.getTime());
            if (completionDates.contains(dateStr)) {
                count++;
            }
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        return count;
    }

    private void updateCharts() {
        if (habits.isEmpty()) return;

        updateLineChart();
        updateBarChart();
        updatePieChart();
    }

    private void updateLineChart() {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        for (int i = 29; i >= 0; i--) {
            Calendar tempCal = (Calendar) cal.clone();
            tempCal.add(Calendar.DAY_OF_YEAR, -i);
            String dateStr = DateUtils.dateToString(tempCal.getTime());

            int completions = 0;
            if (selectedHabit != null) {
                if (selectedHabit.isCompletedOnDate(dateStr)) {
                    completions = 1;
                }
            } else {
                for (HabitEntity habit : habits) {
                    if (habit.isCompletedOnDate(dateStr)) {
                        completions++;
                    }
                }
            }

            entries.add(new Entry(29 - i, completions));
            if (i % 5 == 0) {
                labels.add(DateUtils.getDayMonthShort(tempCal.getTime()));
            } else {
                labels.add("");
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Completions");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setValueTextSize(10f);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleRadius(4f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChart.animateX(2000);
        lineChart.invalidate();
    }

    private void updateBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

        Calendar cal = Calendar.getInstance();
        int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = currentDayOfWeek - Calendar.MONDAY;
        if (daysToSubtract < 0) daysToSubtract += 7;
        cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract);

        for (int i = 0; i < 7; i++) {
            String dateStr = DateUtils.dateToString(cal.getTime());
            int completions = 0;

            if (selectedHabit != null) {
                if (selectedHabit.isCompletedOnDate(dateStr)) {
                    completions = 1;
                }
            } else {
                for (HabitEntity habit : habits) {
                    if (habit.isCompletedOnDate(dateStr)) {
                        completions++;
                    }
                }
            }

            entries.add(new BarEntry(i, completions));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        BarDataSet dataSet = new BarDataSet(entries, "This Week");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.animateY(2000);
        barChart.invalidate();
    }

    private void updatePieChart() {
        Map<String, Integer> categoryMap = new HashMap<>();

        String[] categories = {"Health", "Productivity", "Fitness", "Learning", "Other"};
        for (String cat : categories) {
            categoryMap.put(cat, 0);
        }

        if (selectedHabit != null) {
            List<PieEntry> entries = new ArrayList<>();
            int completed = selectedHabit.getCurrentStreak();
            int total = Math.max(1, completed + 5);
            entries.add(new PieEntry(completed, "Completed"));
            entries.add(new PieEntry(total - completed, "Remaining"));

            PieDataSet dataSet = new PieDataSet(entries, "Progress");
            dataSet.setColors(Color.parseColor("#4CAF50"), Color.parseColor("#E0E0E0"));
            dataSet.setValueTextSize(14f);

            PieData pieData = new PieData(dataSet);
            pieChart.setData(pieData);
        } else {
            for (HabitEntity habit : habits) {
                String category = habit.getCategory();
                if (category == null) category = "Other";
                categoryMap.put(category, categoryMap.getOrDefault(category, 0) + 1);
            }

            List<PieEntry> entries = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
                if (entry.getValue() > 0) {
                    entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                }
            }

            PieDataSet dataSet = new PieDataSet(entries, "Categories");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            dataSet.setValueTextSize(14f);

            PieData pieData = new PieData(dataSet);
            pieChart.setData(pieData);
        }

        pieChart.animateY(2000);
        pieChart.invalidate();
    }
}
