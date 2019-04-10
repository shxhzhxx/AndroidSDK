package com.shxhzhxx.sdk.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

/**
 * this viewGroup will intercept all touch event.
 * it is useful when a recyclerView wants to display without handle any event.
 */
open class InterceptFrameLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    var interceptor: ((ev: MotionEvent) -> Boolean) = { true }
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return interceptor(ev) || super.onInterceptTouchEvent(ev)
    }
}
