package com.shxhzhxx.sdk.activity

import android.app.Activity
import java.lang.ref.WeakReference

private val activities = mutableListOf<WeakReference<Activity>>()

fun addActivity(activity: Activity) {
    if (activities.none { it.get() == activity }) {
        activities.add(WeakReference(activity))
    }
}

fun removeActivity(activity: Activity) {
    activities.removeAll { it.get() == null || it.get() == activity }
}

fun lastActivity() = activities.findLast { it.get() != null }?.get()

fun finishAllActivities(without: Activity? = null) {
    activities.filter { it.get() != without }.forEach { it.get()?.finish() }
    activities.removeAll { it.get() == null || it.get() != without }
}
