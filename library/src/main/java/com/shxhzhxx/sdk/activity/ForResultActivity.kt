package com.shxhzhxx.sdk.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

abstract class ForResultActivity : PermissionRequestActivity() {
    private var resultCallbacks = HashMap<Int, (Int, Intent?) -> Unit>()

    fun startActivityForResult(intent: Intent, onResult: (resultCode: Int, data: Intent?) -> Unit) {
        val requestCode = getRequestCode(resultCallbacks)
        resultCallbacks[requestCode] = onResult
        startActivityForResult(intent, requestCode)
    }

    suspend fun startActivityForResultCoroutine(intent: Intent, onFailure: (() -> Unit)? = null): Intent? =
            try {
                suspendCancellableCoroutine { continuation ->
                    startActivityForResult(intent) { resultCode, data ->
                        if (resultCode == Activity.RESULT_OK)
                            continuation.resume(data)
                        else
                            continuation.resumeWithException(CancellationException())
                    }
                }
            } catch (e: CancellationException) {
                onFailure?.invoke()
                throw e
            }

    final override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        resultCallbacks.remove(requestCode)!!(resultCode, data)
    }


    @Deprecated("Use callback version to avoid conflict.", ReplaceWith("startActivityForResult(intent){resultCode,data->}"))
    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
    }

    @Deprecated("Use callback version to avoid conflict.", ReplaceWith("startActivityForResult(intent){resultCode,data->}"))
    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        super.startActivityForResult(intent, requestCode, options)
    }
}
