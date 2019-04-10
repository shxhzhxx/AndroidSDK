package com.shxhzhxx.sdk

import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import android.os.Build
import com.shxhzhxx.imageloader.ImageLoader
import com.shxhzhxx.sdk.network.Net
import com.shxhzhxx.sdk.utils.initParams
import com.shxhzhxx.sdk.utils.initRes
import com.shxhzhxx.sdk.utils.initToast


lateinit var imageLoader: ImageLoader private set
lateinit var net: Net private set

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class OnProcessCreate(val processNames: Array<String>)

open class Application : android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        val processName = (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .runningAppProcesses.find { it.pid == android.os.Process.myPid() }?.processName
        javaClass.methods.filter { method -> method.annotations.any { it is OnProcessCreate && processName in it.processNames } }
                .forEach { it(this) }
    }

    @OnProcessCreate([BuildConfig.APPLICATION_ID])
    open fun onMainProcessCreate() {
        imageLoader = ImageLoader(contentResolver, cacheDir)
        net = Net(this)
        initParams(this)
        initToast(this)
        initRes(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL || level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW || level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                imageLoader.bitmapLoader.trimMemory(false)
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            imageLoader.bitmapLoader.trimMemory(true)
        }
    }
}
