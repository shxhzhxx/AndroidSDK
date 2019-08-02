package com.shxhzhxx.sdk.activity

import android.app.Activity
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.PopupWindow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import java.lang.ref.WeakReference

private val activities = mutableListOf<WeakReference<Activity>>()

fun Activity.addActivity() {
    if (activities.none { it.get() == this }) {
        activities.add(WeakReference(this))
    }
}

fun Activity.removeActivity() {
    activities.removeAll { it.get() == null || it.get() == this }
}

fun lastActivity() = activities.findLast { it.get() != null }?.get()

fun finishAllActivities(without: Activity? = null) {
    activities.filter { it.get() != without }.forEach { it.get()?.finish() }
    activities.removeAll { it.get() == null || it.get() != without }
}

fun CoroutineActivity.keyboard(onHeight: (Int) -> Unit) {
    val popupWindow = PopupWindow(this)
    val popupView = View(this)
    popupWindow.contentView = popupView
    popupWindow.softInputMode = LayoutParams.SOFT_INPUT_ADJUST_RESIZE or LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
    popupWindow.inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
    popupWindow.width = 0
    popupWindow.height = LayoutParams.MATCH_PARENT
    popupWindow.setBackgroundDrawable(ColorDrawable(0))
    popupView.viewTreeObserver.addOnGlobalLayoutListener {
        val screenSize = Point()
        windowManager.defaultDisplay.getSize(screenSize)
        val rect = Rect()
        popupView.getWindowVisibleDisplayFrame(rect)
        onHeight(screenSize.y - rect.bottom)
    }

    var job: Job? = null
    lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            job = launch {
                withTimeout(1000) { while (window.decorView.windowToken == null) yield() }
                popupWindow.showAtLocation(window.decorView, Gravity.NO_GRAVITY, 0, 0)
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            popupWindow.dismiss()
            job?.cancel()
        }
    })
}
