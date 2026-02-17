package com.sajoldev.habittracker.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sajoldev.habittracker.R;
import com.sajoldev.habittracker.data.HabitEntity;
import com.sajoldev.habittracker.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * HabitAdapter - RecyclerView Adapter for displaying habits
 * Handles item display, click events, and completion animations
 */
public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private List<HabitEntity> habits = new ArrayList<>();
    private OnHabitClickListener listener;
    private Date selectedDate;

    /**
     * Interface for handling habit interactions
     */
    public interface OnHabitClickListener {
        void onHabitClick(HabitEntity habit, int position);
        void onHabitLongClick(HabitEntity habit, int position);
        void onCheckClick(HabitEntity habit, int position, boolean isChecked);
    }

    public HabitAdapter(OnHabitClickListener listener) {
        this.listener = listener;
        this.selectedDate = new Date();
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        HabitEntity habit = habits.get(position);
        holder.bind(habit, position);
        
        Animation slideIn = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.item_slide_in);
        slideIn.setStartOffset(position * 100);
        holder.itemView.startAnimation(slideIn);
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    /**
     * Update the list of habits
     */
    public void setHabits(List<HabitEntity> habits) {
        this.habits = habits;
        notifyDataSetChanged();
    }

    /**
     * Get habit at position
     */
    public HabitEntity getHabitAt(int position) {
        return habits.get(position);
    }

    /**
     * Set the currently selected date for viewing
     */
    public void setSelectedDate(Date date) {
        this.selectedDate = date;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for habit items
     */
    class HabitViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView iconImageView;
        private ImageView checkImageView;
        private TextView nameTextView;
        private TextView goalTextView;
        private TextView streakTextView;
        private View colorIndicator;

        HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            iconImageView = itemView.findViewById(R.id.iconImageView);
            checkImageView = itemView.findViewById(R.id.checkImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            goalTextView = itemView.findViewById(R.id.goalTextView);
            streakTextView = itemView.findViewById(R.id.streakTextView);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
        }

        void bind(HabitEntity habit, int position) {
            // Set habit name and goal
            nameTextView.setText(habit.getName());
            goalTextView.setText(habit.getGoal());

            // Set streak display
            String streakText = "🔥 " + habit.getCurrentStreak() + " day streak";
            if (habit.getCurrentStreak() != 1) {
                streakText = "🔥 " + habit.getCurrentStreak() + " day streak";
            }
            streakTextView.setText(streakText);

            // Set color indicator
            colorIndicator.setBackgroundColor(habit.getColor());

            // Set icon
            iconImageView.setImageResource(habit.getIconResourceId());

            // Check if viewing today or a past/future date
            String dateStr = DateUtils.dateToString(selectedDate);
            boolean isToday = DateUtils.isToday(selectedDate);
            boolean isCompleted = habit.isCompletedOnDate(dateStr);

            // Update completion UI
            updateCompletionUI(isCompleted);

            // Enable/disable click based on date
            checkImageView.setEnabled(isToday);
            checkImageView.setAlpha(isToday ? 1.0f : 0.5f);

            // Click listener for checkbox
            checkImageView.setOnClickListener(v -> {
                if (listener != null && isToday) {
                    // Animate the checkmark
                    animateCheck(v, !isCompleted, () -> {
                        listener.onCheckClick(habit, position, !isCompleted);
                    });
                }
            });

            // Click listener for the whole card
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHabitClick(habit, position);
                }
            });

            // Long click listener for edit/delete
            cardView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onHabitLongClick(habit, position);
                    return true;
                }
                return false;
            });
        }

        /**
         * Update UI based on completion status
         */
        private void updateCompletionUI(boolean isCompleted) {
            if (isCompleted) {
                checkImageView.setImageResource(R.drawable.ic_check_circle);
                checkImageView.setColorFilter(cardView.getContext().getResources().getColor(R.color.colorSuccess));
                cardView.setAlpha(0.7f);
            } else {
                checkImageView.setImageResource(R.drawable.ic_check_circle_outline);
                checkImageView.setColorFilter(cardView.getContext().getResources().getColor(R.color.colorGray));
                cardView.setAlpha(1.0f);
            }
        }

        /**
         * Animate the checkmark when clicked
         * Shows a scaling animation for visual feedback
         */
        private void animateCheck(View view, boolean isCompleting, Runnable onComplete) {
            // Scale up
            ObjectAnimator scaleUp = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f);
            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.3f);
            
            scaleUp.setDuration(150);
            scaleUpY.setDuration(150);
            
            // Scale down
            ObjectAnimator scaleDown = ObjectAnimator.ofFloat(view, "scaleX", 1.3f, 1f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.3f, 1f);
            
            scaleDown.setDuration(150);
            scaleDownY.setDuration(150);
            
            // Chain animations
            scaleUp.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // Update icon before scaling down
                    if (isCompleting) {
                        checkImageView.setImageResource(R.drawable.ic_check_circle);
                        checkImageView.setColorFilter(cardView.getContext().getResources().getColor(R.color.colorSuccess));
                        // Play completion sound
                        playCompletionSound();
                    } else {
                        checkImageView.setImageResource(R.drawable.ic_check_circle_outline);
                        checkImageView.setColorFilter(cardView.getContext().getResources().getColor(R.color.colorGray));
                    }
                    
                    scaleDown.start();
                    scaleDownY.start();
                }
            });
            
            scaleDown.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });
            
            scaleUp.start();
            scaleUpY.start();
        }

        private void playCompletionSound() {
            try {
                MediaPlayer mediaPlayer = MediaPlayer.create(cardView.getContext(), android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
