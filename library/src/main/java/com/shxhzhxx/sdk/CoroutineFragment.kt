package com.shxhzhxx.sdk

import androidx.fragment.app.Fragment
import com.shxhzhxx.sdk.activity.post
import com.shxhzhxx.sdk.activity.postCoroutine
import com.shxhzhxx.sdk.network.CODE_CANCELED
import com.shxhzhxx.sdk.network.CODE_MULTIPLE_REQUEST
import com.shxhzhxx.sdk.network.CODE_UNATTACHED_FRAGMENT
import com.shxhzhxx.sdk.network.PostType
import com.shxhzhxx.sdk.utils.toast
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

open class CoroutineFragment : Fragment(), CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}

inline fun <reified T> Fragment.post(url: String, params: JSONObject? = null, postType: PostType = PostType.FORM,
                                     noinline onResponse: ((msg: String, data: T) -> Unit)? = null,
                                     noinline onFailure: ((errno: Int, msg: String) -> Unit)? = null): Int {
    return activity?.post(url, params, postType, onResponse, onFailure)
            ?: run { onFailure?.invoke(CODE_UNATTACHED_FRAGMENT, net.getMsg(CODE_UNATTACHED_FRAGMENT));-1 }
}

suspend inline fun <reified T> Fragment.postCoroutine(url: String, params: JSONObject? = null, postType: PostType = PostType.FORM,
                                                      noinline onResponse: ((msg: String, data: T) -> Unit)? = null,
                                                      onFailure: (errno: Int, msg: String) -> Unit = { errno, msg ->
                                                          when (errno) {
                                                              CODE_MULTIPLE_REQUEST, CODE_CANCELED -> {
                                                              }
                                                              else -> toast(msg)
                                                          }
                                                      }): T {
    return activity?.postCoroutine(url, params, postType, onResponse, onFailure)
            ?: run { onFailure(CODE_UNATTACHED_FRAGMENT, net.getMsg(CODE_UNATTACHED_FRAGMENT));throw CancellationException() }
}