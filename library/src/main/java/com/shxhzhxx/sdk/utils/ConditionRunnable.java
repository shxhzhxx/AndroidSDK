package com.shxhzhxx.sdk.utils;

import androidx.annotation.IntRange;

public abstract class ConditionRunnable implements Runnable {
    private final boolean[] conditions;

    public ConditionRunnable(@IntRange(from = 1) int size) {
        conditions = new boolean[size];
    }

    public void setCond(int index, boolean cond) {
        conditions[index] = cond;
        for (boolean e : conditions)
            if (!e)
                return;
        run();
    }

    public void reset() {
        for (int i = 0; i < conditions.length; ++i) {
            conditions[i] = false;
        }
    }
}
