package com.sajoldev.habittracker.utils;

import android.animation.ValueAnimator;
import android.widget.TextView;

public class CountAnimation {

    public static void animateCount(TextView textView, int fromValue, int toValue, int duration) {
        ValueAnimator animator = ValueAnimator.ofInt(fromValue, toValue);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            textView.setText(animation.getAnimatedValue().toString());
        });
        animator.start();
    }

    public static void animateCountWithSuffix(TextView textView, int fromValue, int toValue, String suffix, int duration) {
        ValueAnimator animator = ValueAnimator.ofInt(fromValue, toValue);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            textView.setText(animation.getAnimatedValue().toString() + suffix);
        });
        animator.start();
    }

    public static void animateCountFraction(TextView textView, int fromValue, int toValue, int total, int duration) {
        ValueAnimator animator = ValueAnimator.ofInt(fromValue, toValue);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            int current = (int) animation.getAnimatedValue();
            textView.setText(current + "/" + total);
        });
        animator.start();
    }
}
