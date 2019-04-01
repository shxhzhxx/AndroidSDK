package com.shxhzhxx.sdk.activity

import android.app.Activity
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
