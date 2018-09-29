package com.shxhzhxx.sdk.ui.behavior;

import android.content.Context;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

public class NestScrollBehavior extends CoordinatorLayout.Behavior<View> {
    private View mDependency;

    public NestScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        if (mDependency == null && dependency instanceof BottomLayout) {
            mDependency = dependency;
        }
        return mDependency == dependency;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        int offset = dependency.getTop() - child.getBottom();
        if (offset != 0) {
            int top = child.getTop();
            if (top + offset > 0)
                offset = -top;
            if (offset != 0) {
                ViewCompat.offsetTopAndBottom(child, offset);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        if (mDependency != null) {
            int offset = mDependency.getMeasuredHeight() - (parent.getMeasuredHeight() - child.getMeasuredHeight());
            if (offset > 0) {
                ViewCompat.offsetTopAndBottom(mDependency, offset);
            }
        }
        return super.onLayoutChild(parent, child, layoutDirection);
    }
}
