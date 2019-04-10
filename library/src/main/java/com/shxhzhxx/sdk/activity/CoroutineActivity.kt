package com.shxhzhxx.sdk.activity

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

abstract class CoroutineActivity : AppCompatActivity(), CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        removeActivity()
    }
}

@RequiresApi(Build.VERSION_CODES.M)
fun Activity.setStatusBarColor(color: Int, lightStatusBar: Boolean = true) {
    window.statusBarColor = color
    window.decorView.systemUiVisibility = if (lightStatusBar) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else View.SYSTEM_UI_FLAG_VISIBLE
}

@RequiresApi(Build.VERSION_CODES.M)
fun Activity.fullscreen(hideStatusBar: Boolean = false, lightStatusBar: Boolean = false) {
    if (hideStatusBar) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
    } else {
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or (if (lightStatusBar) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else View.SYSTEM_UI_FLAG_VISIBLE)
    }
}
