package com.sajoldev.habittracker.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sajoldev.habittracker.R;
import com.sajoldev.habittracker.utils.AchievementManager;

import java.util.List;

public class AchievementsFragment extends Fragment {

    private AchievementManager achievementManager;
    private GridLayout achievementsGrid;
    private TextView tvUnlockedCount;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievements, container, false);

        achievementManager = new AchievementManager(requireContext());
        achievementsGrid = view.findViewById(R.id.achievementsGrid);
        tvUnlockedCount = view.findViewById(R.id.tvUnlockedCount);
        progressBar = view.findViewById(R.id.progressBar);

        loadAchievements();

        return view;
    }

    private void loadAchievements() {
        achievementsGrid.removeAllViews();
        List<AchievementManager.Achievement> achievements = achievementManager.getAllAchievements();

        int unlockedCount = 0;
        for (int i = 0; i < achievements.size(); i++) {
            AchievementManager.Achievement achievement = achievements.get(i);
            boolean unlocked = achievementManager.isUnlocked(achievement.id);
            if (unlocked) unlockedCount++;
            
            View achievementView = createAchievementView(achievement, unlocked);
            achievementsGrid.addView(achievementView);
            
            animateAchievement(achievementView, i, unlocked);
        }

        animateProgress(unlockedCount, achievements.size());
    }

    private void animateAchievement(View view, int position, boolean unlocked) {
        view.setAlpha(0f);
        view.setScaleX(0.5f);
        view.setScaleY(0.5f);

        new Handler().postDelayed(() -> {
            ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(alpha, scaleX, scaleY);
            animatorSet.setDuration(500);
            animatorSet.setInterpolator(new OvershootInterpolator(1.2f));
            animatorSet.setStartDelay(position * 100);
            animatorSet.start();

            if (unlocked) {
                new Handler().postDelayed(() -> {
                    playPulseAnimation(view);
                }, 500 + position * 100);
            }
        }, position * 150);
    }

    private void playPulseAnimation(View view) {
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.15f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.15f);
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.15f, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.15f, 1f);

        scaleUpX.setDuration(300);
        scaleUpY.setDuration(300);
        scaleDownX.setDuration(300);
        scaleDownY.setDuration(300);

        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(scaleUpX, scaleUpY);
        scaleUp.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(scaleDownX, scaleDownY);
        scaleDown.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleUp.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                scaleDown.start();
            }
        });

        scaleUp.start();
    }

    private void animateProgress(int unlockedCount, int totalCount) {
        tvUnlockedCount.setText("0 / " + totalCount);
        
        ValueAnimator countAnimator = ValueAnimator.ofInt(0, unlockedCount);
        countAnimator.setDuration(1500);
        countAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        countAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            tvUnlockedCount.setText(value + " / " + totalCount);
        });
        countAnimator.start();

        ValueAnimator progressAnimator = ValueAnimator.ofInt(0, unlockedCount);
        progressAnimator.setDuration(1500);
        progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            progressBar.setProgress(value);
        });
        progressAnimator.start();
    }

    private View createAchievementView(AchievementManager.Achievement achievement, boolean unlocked) {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(android.view.Gravity.CENTER);
        container.setPadding(16, 16, 16, 16);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        container.setLayoutParams(params);

        TextView iconView = new TextView(getContext());
        iconView.setText(achievement.icon);
        iconView.setTextSize(40);
        iconView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        iconView.setAlpha(unlocked ? 1.0f : 0.3f);
        container.addView(iconView);

        TextView titleView = new TextView(getContext());
        titleView.setText(achievement.title);
        titleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        titleView.setTextSize(12);
        titleView.setTextColor(unlocked ? getResources().getColor(R.color.colorPrimary) : 0xFF757575);
        titleView.setTypeface(null, unlocked ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        container.addView(titleView);

        return container;
    }
}
