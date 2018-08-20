package com.shxhzhxx.sdk.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * this viewGroup will intercept all touch event.
 * it is useful when a recyclerView wants to display without handle any event.
 */
public class InterceptFrameLayout extends FrameLayout {
    public interface InterceptListener {
        boolean onInterceptTouchEvent(MotionEvent ev);
    }

    private InterceptListener mListener = null;

    public InterceptFrameLayout(@NonNull Context context) {
        super(context);
    }

    public InterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setInterceptListener(InterceptListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mListener == null || mListener.onInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev);
    }
}
