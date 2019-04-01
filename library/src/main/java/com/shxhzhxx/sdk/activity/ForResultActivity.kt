package com.shxhzhxx.sdk.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class ActivityResult(val resultCode: Int, val data: Intent?)

infix fun Int.to(that: Intent?): ActivityResult = ActivityResult(this, that)
abstract class ForResultActivity : PermissionRequestActivity() {
    private var resultCallbacks = HashMap<Int, (Int, Intent?) -> Unit>()

    fun startActivityForResult(intent: Intent, onResult: (resultCode: Int, data: Intent?) -> Unit) {
        val requestCode = getRequestCode(resultCallbacks)
        resultCallbacks[requestCode] = onResult
        startActivityForResult(intent, requestCode)
    }

    suspend fun startActivityForResultCoroutine(intent: Intent): ActivityResult =
            try {
                suspendCancellableCoroutine { continuation ->
                    startActivityForResult(intent) { resultCode, data -> continuation.resume(resultCode to data) }
                }
            } catch (e: CancellationException) {
                Activity.RESULT_CANCELED to null
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
