package com.shxhzhxx.sdk.activity

import android.content.Intent
import android.os.Bundle

abstract class ForResultActivity : PermissionRequestActivity() {
    var resultCallbacks = HashMap<Int, (Int, Intent?) -> Unit>()

    fun startActivityForResult(intent: Intent, onResult: (resultCode: Int, data: Intent?) -> Unit) {
        val requestCode = getRequestCode(resultCallbacks)
        resultCallbacks[requestCode] = onResult
        startActivityForResult(intent, requestCode)
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
