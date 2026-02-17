package com.sajoldev.habittracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sajoldev.habittracker.R;
import com.sajoldev.habittracker.utils.DateUtils;

import java.util.Date;

/**
 * DatePickerAdapter - Horizontal RecyclerView Adapter for date selection
 * Shows a scrollable list of dates at the top of the main screen
 */
public class DatePickerAdapter extends RecyclerView.Adapter<DatePickerAdapter.DateViewHolder> {

    private Date[] dates;
    private int selectedPosition;
    private OnDateSelectedListener listener;

    /**
     * Interface for date selection callbacks
     */
    public interface OnDateSelectedListener {
        void onDateSelected(Date date, int position);
    }

    public DatePickerAdapter(Date[] dates, OnDateSelectedListener listener) {
        this.dates = dates;
        this.listener = listener;
        this.selectedPosition = findTodayPosition(); // Select today by default
    }
    
    /**
     * Find the position of today in the dates array
     */
    private int findTodayPosition() {
        Date today = new Date();
        String todayStr = DateUtils.dateToString(today);
        for (int i = 0; i < dates.length; i++) {
            if (DateUtils.dateToString(dates[i]).equals(todayStr)) {
                return i;
            }
        }
        return dates.length / 2; // Fallback to middle if today not found
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        holder.bind(dates[position], position);
    }

    @Override
    public int getItemCount() {
        return dates.length;
    }

    /**
     * Set selected position
     */
    public void setSelectedPosition(int position) {
        int previousSelected = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previousSelected);
        notifyItemChanged(selectedPosition);
    }

    /**
     * Get selected date
     */
    public Date getSelectedDate() {
        return dates[selectedPosition];
    }

    /**
     * Get position for a specific date
     */
    public int getPositionForDate(Date date) {
        String targetDate = DateUtils.dateToString(date);
        for (int i = 0; i < dates.length; i++) {
            if (DateUtils.dateToString(dates[i]).equals(targetDate)) {
                return i;
            }
        }
        return -1;
    }

    class DateViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView dayTextView;
        private TextView dateTextView;

        DateViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.dateCardView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }

        void bind(Date date, int position) {
            // Set day name (Mon, Tue, etc.)
            dayTextView.setText(DateUtils.getDayName(date));
            
            // Set date number
            dateTextView.setText(String.valueOf(getDayOfMonth(date)));

            // Highlight today
            if (DateUtils.isToday(date)) {
                dayTextView.setText("TODAY");
            }

            // Update selection state
            boolean isSelected = position == selectedPosition;
            updateSelectionState(isSelected);

            // Click listener
            cardView.setOnClickListener(v -> {
                if (position != selectedPosition) {
                    setSelectedPosition(position);
                    if (listener != null) {
                        listener.onDateSelected(date, position);
                    }
                }
            });
        }

        private void updateSelectionState(boolean isSelected) {
            if (isSelected) {
                cardView.setCardBackgroundColor(cardView.getContext().getResources().getColor(R.color.colorPrimary));
                dayTextView.setTextColor(cardView.getContext().getResources().getColor(android.R.color.white));
                dateTextView.setTextColor(cardView.getContext().getResources().getColor(android.R.color.white));
                cardView.setElevation(8f);
            } else {
                cardView.setCardBackgroundColor(cardView.getContext().getResources().getColor(android.R.color.white));
                dayTextView.setTextColor(cardView.getContext().getResources().getColor(R.color.colorTextSecondary));
                dateTextView.setTextColor(cardView.getContext().getResources().getColor(R.color.colorTextPrimary));
                cardView.setElevation(2f);
            }
        }

        private int getDayOfMonth(Date date) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(date);
            return cal.get(java.util.Calendar.DAY_OF_MONTH);
        }
    }
}
