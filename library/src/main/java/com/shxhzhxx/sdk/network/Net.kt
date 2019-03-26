package com.shxhzhxx.sdk.network

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.shxhzhxx.sdk.R
import com.shxhzhxx.urlloader.TaskManager
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit

const val TAG = "Net"
const val CODE_OK = 0
const val CODE_NO_AVAILABLE_NETWORK = -1
const val CODE_TIMEOUT = -2
const val CODE_GET_PARAM_FAILED = -3
const val CODE_UNEXPECTED_RESPONSE = -4
const val CODE_CANCELED = -5
const val CODE_MULTIPLE_REQUEST = -6

private val DATA_TYPE_FORM = MediaType.parse("application/x-www-form-urlencoded;charset=utf-8")//form表单
private val DATA_TYPE_FILE = MediaType.parse("application/octet-stream")
private val DATA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8")

class Net(context: Context) : TaskManager<(errno: Int, msg: String, data: String?) -> Unit, Unit>() {
    private val codeMsg = mapOf(
            CODE_OK to context.resources.getString(R.string.err_msg_ok),
            CODE_NO_AVAILABLE_NETWORK to context.resources.getString(R.string.err_msg_no_available_network),
            CODE_TIMEOUT to context.resources.getString(R.string.err_msg_timeout),
            CODE_GET_PARAM_FAILED to context.resources.getString(R.string.err_msg_get_param_failed),
            CODE_UNEXPECTED_RESPONSE to context.resources.getString(R.string.err_msg_unexpected_response),
            CODE_CANCELED to context.resources.getString(R.string.err_msg_cancelled),
            CODE_MULTIPLE_REQUEST to context.resources.getString(R.string.err_msg_multiple_request)
    )
    private val defaultErrorMessage = context.resources.getString(R.string.err_msg_default)
    private val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val okHttpClient = OkHttpClient.Builder()
            .writeTimeout(3, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .build()
    private val lifecycleSet = HashSet<Lifecycle>()

    val isNetworkAvailable get() = connMgr.activeNetworkInfo?.isConnected == true
    val isWifiAvailable get() = isNetworkAvailable && connMgr.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI

    fun getMsg(errno: Int, defValue: String = defaultErrorMessage) = codeMsg[errno] ?: defValue

    fun request(key: Any, request: Request, lifecycle: Lifecycle? = null,
                onResult: ((errno: Int, msg: String, data: String?) -> Unit)? = null) {
        if (isRunning(key)) {
            onResult?.invoke(CODE_MULTIPLE_REQUEST, getMsg(CODE_MULTIPLE_REQUEST), null)
            return
        }
        if (lifecycle != null && !lifecycleSet.contains(lifecycle)) {
            lifecycleSet.add(lifecycle)
            lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    lifecycleSet.remove(lifecycle)
                    unregisterByTag(lifecycle)
                }
            })
        }
        asyncStart(key, { Worker(key, request) }, lifecycle, onResult)
    }

    fun postForm(url: String, key: Any, lifecycle: Lifecycle? = null, onParams: ((params: JSONObject) -> JSONObject)? = null,
                 onResult: ((errno: Int, msg: String, data: String?) -> Unit)? = null) {
        request(key, Request.Builder().also { builder ->
            builder.url(url)
            onParams?.invoke(JSONObject())?.let { params ->
                if (params.length() > 0) {
                    builder.post(RequestBody.create(DATA_TYPE_FORM, formatJsonToForm(params)))
                }
            }
        }.build(), lifecycle, onResult)
    }

    fun postJson(url: String, key: Any, lifecycle: Lifecycle? = null, onParams: ((params: JSONObject) -> JSONObject)? = null,
                 onResult: ((errno: Int, msg: String, data: String?) -> Unit)? = null) {
        request(key, Request.Builder().also { builder ->
            builder.url(url)
            onParams?.invoke(JSONObject())?.let { params ->
                if (params.length() > 0) {
                    builder.post(RequestBody.create(DATA_TYPE_JSON, params.toString()))
                }
            }
        }.build(), lifecycle, onResult)
    }

    fun postFile(url: String, key: Any, lifecycle: Lifecycle? = null, file: File,
                 onResult: ((errno: Int, msg: String, data: String?) -> Unit)? = null) {
        request(key, Request.Builder().url(url).post(RequestBody.create(DATA_TYPE_FILE, file)).build(), lifecycle, onResult)
    }

    fun postMultipartForm(url: String, key: Any, lifecycle: Lifecycle? = null, files: List<Pair<String, File>> = emptyList(),
                          onParams: ((params: JSONObject) -> JSONObject)? = null, onResult: ((errno: Int, msg: String, data: String?) -> Unit)? = null) {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        for ((name, file) in files) {
            builder.addFormDataPart(name, file.name, RequestBody.create(DATA_TYPE_FILE, file))
        }
        onParams?.invoke(JSONObject())?.let { params ->
            for (k in params.keys()) {
                builder.addFormDataPart(k, params.getString(k))
            }
        }
        request(key, Request.Builder().url(url).post(builder.build()).build(), lifecycle, onResult)
    }

    private inner class Worker(key: Any, private val request: Request) : Task(key) {
        override fun onCanceled() {
            observers.forEach { it?.invoke(CODE_CANCELED, getMsg(CODE_CANCELED), null) }
        }

        override fun onObserverUnregistered(observer: ((errno: Int, msg: String, data: String?) -> Unit)?) {
            observer?.invoke(CODE_CANCELED, getMsg(CODE_CANCELED), null)
        }

        override fun doInBackground() {
            val call = okHttpClient.newCall(request)
            val (errno, data) = kotlin.run {
                val response = try {
                    call.execute()
                } catch (e: IOException) {
                    Log.e(TAG, "execute IOException: ${e.message}")
                    return@run (if (isNetworkAvailable) CODE_TIMEOUT else CODE_NO_AVAILABLE_NETWORK) to null
                }
                if (!response.isSuccessful) {
                    response.close()
                    Log.e(TAG, "HTTP code ${response.code()}")
                    return@run CODE_UNEXPECTED_RESPONSE to null
                }
                val body = response.body()!!
                try {
                    return@run CODE_OK to body.string()
                } catch (e: IOException) {
                    Log.e(TAG, "read string IOException: ${e.message}")
                    return@run (if (isNetworkAvailable) CODE_TIMEOUT else CODE_NO_AVAILABLE_NETWORK) to null
                } finally {
                    body.close()
                }
            }
            postResult = Runnable {
                observers.forEach { it?.invoke(errno, getMsg(errno), data) }
            }
        }
    }
}

/**
 * 将json格式的请求参数转化成表单格式提交，转化过程中可能对特殊字符转码
 * param json {"k1":"v1","k2":"v2","k3":{"k4":"v4","k5":"v5"}}
 * return k1=v1&k2=v2&k3={"k4":"v4","k5":"v5"}
 */
fun formatJsonToForm(json: JSONObject) = mutableListOf<String>().also { list ->
    for (key in json.keys()) {
        try {
            list.add("${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(json.getString(key), "UTF-8")}")
        } catch (e: JSONException) {
            Log.e("formatJsonToFrom", "unexpected JSONException:${e.message}")
        } catch (e: UnsupportedEncodingException) {
            Log.e("formatJsonToFrom", "unexpected UnsupportedEncodingException:${e.message}")
        }
    }
}.joinToString(separator = "&")

/**
 * 将json格式的字符串，加入分行缩进字符，方便格式化输出。
 * Note: formatJsonString是费时操作
 */
fun formatJsonString(raw: String): String {
    val result = StringBuilder()
    val tabSpace = "    "
    var rowHeader = ""
    var quotation = false
    for (ch in raw) {
        if (quotation && ch != '\"') {
            result.append(ch)
            continue
        }
        if (!quotation && ch.isWhitespace()) {
            continue//跳过引号外的空白符
        }
        when (ch) {
            '\"' -> {
                quotation = !quotation
                result.append(ch)
            }
            '{', '[' -> {
                rowHeader += tabSpace
                result.append(ch).append('\n').append(rowHeader)
            }
            '}', ']' -> {
                rowHeader = rowHeader.drop(tabSpace.length)
                result.append('\n').append(rowHeader).append(ch)
            }
            ',' -> {
                result.append(',').append('\n').append(rowHeader)
            }
            else -> {
                result.append(ch)
            }
        }
    }
    return result.toString()
}