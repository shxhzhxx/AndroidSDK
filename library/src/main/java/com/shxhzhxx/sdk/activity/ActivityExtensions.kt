package com.shxhzhxx.sdk.activity

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.core.type.TypeReference
import com.shxhzhxx.sdk.net
import com.shxhzhxx.sdk.network.*
import com.shxhzhxx.sdk.utils.toast
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private val activities = mutableListOf<WeakReference<Activity>>()

data class Response<T>(
        @JsonAlias("errorCode", "code") val errno: Int,
        @JsonAlias("tips", "errorMsg", "message") val msg: String,
        @JsonAlias("data", "jsondata") val raw: String?,
        @JsonIgnore val data: T?
) {
    val isSuccessful get() = errno == 0
}

inline fun <reified T> FragmentActivity.post(url: String, params: JSONObject = net.defaultParams, postType: PostType = PostType.FORM,
                                             noinline onResponse: ((msg: String, data: T) -> Unit)? = null,
                                             noinline onFailure: ((errno: Int, msg: String) -> Unit)? = null) =
        net.post<Response<T>>(url, RequestKey(url, params), params, lifecycle, postType,
                onResponse = { data ->
                    when{
                        !data.isSuccessful->onFailure?.invoke(data.errno, data.msg)
                        data.data ==null -> onFailure?.invoke(CODE_UNEXPECTED_RESPONSE, net.getMsg(CODE_UNEXPECTED_RESPONSE))
                        else -> onResponse?.invoke(data.msg,data.data)
                    }
//                    if (!data.isSuccessful) {
//                        onFailure?.invoke(data.errno, data.msg)
//                    } else {
//                        try {
//                            onResponse?.invoke(data.msg, mapper.readValue(data.data, T::class.java))
//                        } catch (e: Throwable) {
//                            onFailure?.invoke(CODE_UNEXPECTED_RESPONSE, net.getMsg(CODE_UNEXPECTED_RESPONSE))
//                        }
//                    }
                }, onFailure = onFailure)


suspend inline fun <reified T> FragmentActivity.postCoroutine(url: String, params: JSONObject = net.defaultParams, postType: PostType = PostType.FORM,
                                                              noinline onResponse: ((msg: String, data: T) -> Unit)? = null,
                                                              onFailure: (errno: Int, msg: String) -> Unit = { errno, msg ->
                                                                  when (errno) {
                                                                      CODE_MULTIPLE_REQUEST, CODE_CANCELED -> {
                                                                      }
                                                                      else -> toast(msg)
                                                                  }
                                                              }): T {
    var id: Int? = null
    return try {
        suspendCancellableCoroutine { continuation ->
            id = post<T>(url, params, postType, onResponse = { msg, data -> id = null;onResponse?.invoke(msg, data);continuation.resume(data) },
                    onFailure = { errno, msg -> id = null;continuation.resumeWithException(InternalCancellationException(errno, msg)) })
        }
    } catch (e: CancellationException) {
        net.unregister(id ?: -1)
        if (e is InternalCancellationException)
            onFailure(e.errno, e.msg)
        else
            onFailure(CODE_CANCELED, net.getMsg(CODE_CANCELED))
        throw e
    }
}


fun Activity.addActivity() {
    if (activities.none { it.get() == this }) {
        activities.add(WeakReference(this))
    }
}

fun Activity.removeActivity() {
    activities.removeAll { it.get() == null || it.get() == this }
}

fun lastActivity() = activities.findLast { it.get() != null }?.get()

fun finishAllActivities(without: Activity? = null) {
    activities.filter { it.get() != without }.forEach { it.get()?.finish() }
    activities.removeAll { it.get() == null || it.get() != without }
}
