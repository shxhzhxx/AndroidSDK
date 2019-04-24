package com.shxhzhxx.sdk.utils

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.annotation.IntRange
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.shxhzhxx.imageloader.BitmapLoader
import com.shxhzhxx.imageloader.ImageLoader
import com.shxhzhxx.imageloader.isLaidOutCompat
import com.shxhzhxx.urlloader.UrlLoader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun UrlLoader.loadCoroutine(url: String, onFailure: (() -> Unit)? = null, onProgress: ((total: Long, current: Long, speed: Long) -> Unit)? = null): File {
    var id: Int? = null
    return try {
        suspendCancellableCoroutine { continuation ->
            id = load(url,
                    onLoad = { id = null; continuation.resume(it) },
                    onFailure = { id = null;continuation.resumeWithException(CancellationException()) },
                    onCancel = { id = null;continuation.resumeWithException(CancellationException()) }, onProgress = onProgress)
        }
    } catch (e: CancellationException) {
        unregister(id ?: -1)
        onFailure?.invoke()
        throw e
    }
}

suspend fun BitmapLoader.loadCoroutine(path: String, @IntRange(from = 0) width: Int = 0, @IntRange(from = 0) height: Int = 0,
                                       centerCrop: Boolean = true, onFailure: (() -> Unit)? = null): Bitmap {
    var id: Int? = null
    return try {
        suspendCancellableCoroutine { continuation ->
            id = load(path, width, height, centerCrop,
                    onLoad = { id = null;continuation.resume(it) },
                    onCancel = { id = null;continuation.resumeWithException(CancellationException()) },
                    onFailure = { id = null;continuation.resumeWithException(CancellationException()) })
        }
    } catch (e: CancellationException) {
        unregister(id ?: -1)
        onFailure?.invoke()
        throw e
    }
}

suspend fun ImageLoader.loadCoroutine(iv: ImageView, path: String?,
                                      lifecycle: Lifecycle? = iv.context.let { if (it is FragmentActivity) it.lifecycle else null },
                                      centerCrop: Boolean = true,
                                      width: Int? = if (iv.isLaidOutCompat) iv.width else null,
                                      height: Int? = if (iv.isLaidOutCompat) iv.height else null,
                                      waitForLayout: Boolean = false,
                                      placeholder: Int? = 0,// pass 0 will clear current drawable before load
                                      error: Int? = null,
                                      transformation: ((Bitmap) -> Bitmap)? = null) {
    var id: Int? = null
    return try {
        suspendCancellableCoroutine { continuation ->
            val finally = { id = null;continuation.resume(Unit) }
            id = load(iv, path, lifecycle, centerCrop, width, height, waitForLayout, placeholder, error, transformation, finally, finally, finally)
        }
    } catch (e: CancellationException) {
        unregister(id ?: -1)
        throw e
    }
}