package com.shxhzhxx.sdk.activity

import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


abstract class PermissionRequestActivity : CoroutineActivity() {
    private class MyViewModel : ViewModel() {
        val permissionCallbacks = mutableMapOf<Int, Holder>()
    }

    private class Holder(val onGrant: (() -> Unit)?, val onDeny: ((List<String>) -> Unit)?, val onCancel: (() -> Unit)?)

    private val vm by lazy { ViewModelProviders.of(this).get(MyViewModel::class.java) }

    fun requestPermissions(permissions: List<String>, onGrant: (() -> Unit)? = null,
                           onDeny: ((deniedPermissions: List<String>) -> Unit)? = null,
                           onCancel: (() -> Unit)? = null,
                           onShouldExplain: ((explainPermissions: List<String>, continuation: (accepted: Boolean) -> Unit) -> Unit)? = null) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || permissions.isEmpty()
                || permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) {
            onGrant?.invoke()
        } else {
            val request = {
                requestPermissions(permissions.toTypedArray(), vm.permissionCallbacks.add(Holder(onGrant, onDeny, onCancel)))
            }

            val shouldExplanations = permissions.filter { shouldShowRequestPermissionRationale(it) }
            if (shouldExplanations.isEmpty() || onShouldExplain == null) {
                request()
            } else {
                onShouldExplain(shouldExplanations) { accepted ->
                    if (accepted) request() else onDeny?.invoke(permissions.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED })
                }
            }
        }
    }

    suspend fun requestPermissionsCoroutine(permissions: List<String>, onFailure: (() -> Unit)? = null) {
        try {
            suspendCancellableCoroutine<Unit> { continuation ->
                requestPermissions(permissions, { continuation.resume(Unit) }, { continuation.resumeWithException(CancellationException()) },
                        { continuation.resumeWithException(CancellationException()) })
            }
        } catch (e: CancellationException) {
            onFailure?.invoke()
            throw e
        }
    }


    final override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val callback = vm.permissionCallbacks.remove(requestCode) ?: return
        // If request is cancelled, the result arrays are empty.
        if (grantResults.isEmpty() || grantResults.size != permissions.size) {
            callback.onCancel?.invoke()
        } else {
            val deniedPermissions = permissions.filterIndexed { index, _ -> grantResults[index] != PackageManager.PERMISSION_GRANTED }
            if (deniedPermissions.isEmpty()) {
                callback.onGrant?.invoke()
            } else {
                callback.onDeny?.invoke(deniedPermissions)
            }
        }
    }
}

fun <T> MutableMap<Int, T>.add(value: T): Int {
    var code = 0
    while (containsKey(code)) {
        ++code
    }
    put(code, value)
    return code
}
