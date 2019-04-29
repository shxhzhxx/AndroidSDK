package com.shxhzhxx.sdk.activity

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import com.shxhzhxx.sdk.net
import com.shxhzhxx.sdk.network.CODE_CANCELED
import com.shxhzhxx.sdk.network.CODE_MULTIPLE_REQUEST
import com.shxhzhxx.sdk.network.InternalCancellationException
import com.shxhzhxx.sdk.network.PostType
import com.shxhzhxx.sdk.utils.toast
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
