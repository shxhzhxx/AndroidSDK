package com.shxhzhxx.sdk.activity

import android.content.pm.PackageManager
import android.os.Build
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private class Holder(
        val onGranted: () -> Unit,
        val onDenied: (List<String>) -> Unit,
        val onCanceled: () -> Unit
)

abstract class PermissionRequestActivity : CoroutineActivity() {
    private val permissionCallbacks = HashMap<Int, Holder>()

    /**
     * 其他 Activity 继承 BaseActivity 调用 performRequestPermissions 方法
     *
     * @param permissions 要申请的权限数组
     */
    fun requestPermissions(permissions: List<String>, onGranted: () -> Unit = {},
                           onDenied: (deniedPermissions: List<String>) -> Unit = {},
                           onCanceled: () -> Unit = {},
                           onShouldExplain: (explainPermissions: List<String>, continuation: (accepted: Boolean) -> Unit) -> Unit = { _, continuation -> continuation(true) }) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || permissions.isEmpty()
                || permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) {
            onGranted()
        } else {
            val request = {
                val requestCode = getRequestCode(permissionCallbacks)
                permissionCallbacks[requestCode] = Holder(onGranted, onDenied, onCanceled)
                requestPermissions(permissions.toTypedArray(), requestCode)
            }

            val shouldExplanations = permissions.filter { shouldShowRequestPermissionRationale(it) }
            if (shouldExplanations.isEmpty()) {
                request()
            } else {
                onShouldExplain(shouldExplanations) { accepted ->
                    if (accepted) request() else onDenied(permissions.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED })
                }
            }
        }
    }

    suspend fun requestPermissionsCoroutine(permissions: List<String>, onShouldExplain: (explainPermissions: List<String>, continuation: (accepted: Boolean) -> Unit) -> Unit = { _, continuation -> continuation(true) }): Boolean =
            suspendCoroutine { continuation ->
                requestPermissions(permissions,
                        { continuation.resume(true) },
                        { continuation.resume(false) },
                        { continuation.resume(false) }, onShouldExplain)
            }


    /**
     * 获取一个没有被占用的requestCode
     */
    protected fun <T> getRequestCode(map: Map<Int, T>): Int {
        var code = 0
        while (map.containsKey(code)) {
            ++code
        }
        return code
    }

    /**
     * 申请权限结果的回调
     */
    final override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val callback = permissionCallbacks.remove(requestCode)!!
        // If request is cancelled, the result arrays are empty.
        if (grantResults.isEmpty() || grantResults.size != permissions.size) {
            callback.onCanceled()
        } else {
            val deniedPermissions = permissions.filterIndexed { index, _ -> grantResults[index] != PackageManager.PERMISSION_GRANTED }
            if (deniedPermissions.isEmpty()) {
                callback.onGranted()
            } else {
                callback.onDenied(deniedPermissions)
            }
        }
    }
}
