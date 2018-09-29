package com.shxhzhxx.sdk.ui.behavior;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class BottomLayout extends FrameLayout implements CoordinatorLayout.AttachedBehavior  {
    public BottomLayout(@NonNull Context context) {
        super(context);
    }

    public BottomLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @NonNull
    @Override
    public CoordinatorLayout.Behavior getBehavior() {
        return new BottomBehavior();
    }

    public static class BottomBehavior extends CoordinatorLayout.Behavior<View> {
        public BottomBehavior() {
        }

        public BottomBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
            return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0 && coordinatorLayout.getHeight() - target.getHeight() < child.getHeight();
        }

        @Override
        public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
            if (dyUnconsumed > 0) {
                int available = child.getBottom() - coordinatorLayout.getBottom();
                if (available > 0) {
                    int offset = Math.min(dyUnconsumed, available);
                    ViewCompat.offsetTopAndBottom(child, -offset);
                }
            }
        }

        @Override
        public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
            if (dy < 0) {
                int available = child.getHeight() - (child.getBottom() - coordinatorLayout.getBottom());
                available = Math.min(available, -target.getTop());
                if (available > 0) {
                    int offset = Math.min(-dy, available);
                    if (offset != 0)
                        ViewCompat.offsetTopAndBottom(child, offset);
                    consumed[1] = -offset;
                }
            }
        }
    }
}
