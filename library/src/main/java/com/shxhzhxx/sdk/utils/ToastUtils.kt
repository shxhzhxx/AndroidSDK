package com.shxhzhxx.sdk.utils

import android.content.Context
import android.widget.Toast

import java.lang.ref.WeakReference

private var toast: Toast? = null
private lateinit var contextRef: WeakReference<Context>

fun initToast(context: Context) {
    contextRef = WeakReference(context)
}

fun toast(msg: String) {
    val context = contextRef.get() ?: return
    toast?.cancel()
    toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT).also { it.show() }
}
