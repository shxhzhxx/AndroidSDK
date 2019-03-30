package com.shxhzhxx.sdk.activity

import android.content.Intent
import android.os.Bundle
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class ForResultActivity : PermissionRequestActivity() {
    private var resultCallbacks = HashMap<Int, (Int, Intent?) -> Unit>()

    fun startActivityForResult(intent: Intent, onResult: (resultCode: Int, data: Intent?) -> Unit) {
        val requestCode = getRequestCode(resultCallbacks)
        resultCallbacks[requestCode] = onResult
        startActivityForResult(intent, requestCode)
    }

    suspend fun startActivityForResultCoroutine(intent: Intent): Pair<Int, Intent?> =
            suspendCoroutine { continuation ->
                startActivityForResult(intent) { resultCode, data -> continuation.resume(resultCode to data) }
            }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
