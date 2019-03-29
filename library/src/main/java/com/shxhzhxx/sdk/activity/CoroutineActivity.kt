package com.shxhzhxx.sdk.activity

import android.content.Intent
import com.shxhzhxx.sdk.R
import com.shxhzhxx.sdk.utils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class CoroutineActivity : ForResultActivity(), CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    suspend fun requestPermissionsCoroutine(permissions: List<String>, onDenied: (deniedPermissions: List<String>) -> Unit = { deniedPermissions -> toast(getString(R.string.permission_denied, deniedPermissions.joinToString(separator = "\n"))) },
                                            onCanceled: () -> Unit = { toast(getString(R.string.permission_canceled)) },
                                            onShouldExplain: (explainPermissions: List<String>, request: () -> Unit) -> Unit = { _, request -> request() }) = suspendCoroutine<Unit> { continuation ->
        requestPermissions(permissions, { continuation.resume(Unit) }, onDenied, onCanceled, onShouldExplain)
    }

    suspend fun startActivityForResultCoroutine(intent: Intent) = suspendCoroutine<Pair<Int, Intent?>> { continuation ->
        startActivityForResult(intent) { resultCode, data -> continuation.resume(resultCode to data) }
    }
}