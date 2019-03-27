package com.shxhzhxx.sdk.activity

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.shxhzhxx.sdk.R
import com.shxhzhxx.sdk.utils.toast

private data class Callback(val onPermissionGranted: (() -> Unit), val onPermissionDenied: ((Array<String>) -> Unit))
abstract class PermissionRequestActivity : AppCompatActivity() {
    private val mPermissionCallbacks = HashMap<Int, Callback>()

    /**
     * 其他 Activity 继承 BaseActivity 调用 performRequestPermissions 方法
     *
     * @param permissions 要申请的权限数组
     */
    fun performRequestPermissions(permissions: Array<String>, onPermissionGranted: (() -> Unit) = {},
                                  onPermissionDenied: ((Array<String>) -> Unit) = { deniedPermissions -> toast(getString(R.string.permission_denied, deniedPermissions.joinToString(separator = "\n"))) }) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || permissions.isEmpty()
                || permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            onPermissionGranted()
        } else {
            val requestCode = getRequestCode(mPermissionCallbacks)
            mPermissionCallbacks[requestCode] = Callback(onPermissionGranted, onPermissionDenied)
            ActivityCompat.requestPermissions(this, permissions, requestCode)
        }
    }


    /**
     * 获取一个没有被占用的requestCode
     */
    private fun <T> getRequestCode(map: Map<Int, T>): Int {
        var code = 0
        while (!map.containsKey(code)) {
            ++code
        }
        return code
    }

    /**
     * 申请权限结果的回调
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val callback = mPermissionCallbacks.remove(requestCode)!!
        if (grantResults.isEmpty()) {// If request is cancelled, the result arrays are empty.
            callback.onPermissionDenied(permissions)//TODO 
        } else {
            val deniedPermissions = permissions.filterIndexed { index, _ -> grantResults[index] != PackageManager.PERMISSION_GRANTED }
            if (deniedPermissions.isEmpty()) {
                callback.onPermissionGranted()
            } else {
                callback.onPermissionDenied(deniedPermissions.toTypedArray())
            }
        }
    }
}
