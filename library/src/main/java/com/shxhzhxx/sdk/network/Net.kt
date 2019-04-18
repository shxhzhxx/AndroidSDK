package com.shxhzhxx.sdk.network

import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import com.fasterxml.jackson.datatype.jsonorg.JSONArrayDeserializer
import com.fasterxml.jackson.datatype.jsonorg.JSONObjectDeserializer
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.shxhzhxx.sdk.R
import com.shxhzhxx.urlloader.TaskManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

const val TAG = "Net"
const val CODE_OK = 0
const val CODE_NO_AVAILABLE_NETWORK = -1
const val CODE_TIMEOUT = -2
const val CODE_UNEXPECTED_RESPONSE = -3
const val CODE_CANCELED = -4
const val CODE_MULTIPLE_REQUEST = -5
const val CODE_UNATTACHED_FRAGMENT = -6

val DATA_TYPE_FORM = MediaType.parse("application/x-www-form-urlencoded;charset=utf-8")
val DATA_TYPE_FILE = MediaType.parse("application/octet-stream")
val DATA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8")

enum class PostType {
    FORM, JSON
}

open class JsonStringDeserializer : StringDeserializer() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): String {
        return try {
            super.deserialize(p, ctxt)
        } catch (e: Throwable) {
            when {
                p.currentToken == JsonToken.START_OBJECT ->
                    JSONObjectDeserializer.instance.deserialize(p, ctxt).toString()
                p.currentToken == JsonToken.START_ARRAY ->
                    JSONArrayDeserializer.instance.deserialize(p, ctxt).toString()
                else -> throw e
            }
        }
    }
}

val mapper = ObjectMapper().apply {
    registerKotlinModule()
    registerModule(JsonOrgModule().apply { addDeserializer(String::class.java, JsonStringDeserializer()) })
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
}

class RequestKey(private val url: String, params: JSONObject) {
    private val _params = mapper.readValue<Map<String, Any>>(params.toString())
    override fun equals(other: Any?): Boolean {
        return hashCode() == other?.hashCode()
    }

    override fun hashCode(): Int {
        return url.hashCode() * 31 + _params.hashCode()
    }
}

data class ResponseHeader(
        @JsonAlias("errorCode", "code") val errno: Int,
        @JsonAlias("tips", "errorMsg", "message") val msg: String
) {
    val isSuccessful get() = errno == 0
}

data class ResponseData<T>(
        @JsonAlias("jsondata") val data: T
)

class InternalCancellationException(val errno: Int, val msg: String) : CancellationException()

class Net(context: Context) : TaskManager<(errno: Int, msg: String, data: Any?) -> Unit, Unit>() {
    private val codeMsg = mapOf(
            CODE_OK to context.resources.getString(R.string.err_msg_ok),
            CODE_NO_AVAILABLE_NETWORK to context.resources.getString(R.string.err_msg_no_available_network),
            CODE_TIMEOUT to context.resources.getString(R.string.err_msg_timeout),
            CODE_UNATTACHED_FRAGMENT to context.resources.getString(R.string.err_msg_unattached_fragment),
            CODE_UNEXPECTED_RESPONSE to context.resources.getString(R.string.err_msg_unexpected_response),
            CODE_CANCELED to context.resources.getString(R.string.err_msg_cancelled),
            CODE_MULTIPLE_REQUEST to context.resources.getString(R.string.err_msg_multiple_request)
    )
    var debugMode = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    private val defaultErrorMessage = context.resources.getString(R.string.err_msg_default)
    private val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val okHttpClient = OkHttpClient.Builder()
            .writeTimeout(3, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .build()
    private val lifecycleSet = HashSet<Lifecycle>()
    var defaultParams = JSONObject()

    val isNetworkAvailable get() = connMgr.activeNetworkInfo?.isConnected == true
    val isWifiAvailable get() = isNetworkAvailable && connMgr.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI

    fun getMsg(errno: Int, defValue: String = defaultErrorMessage) = codeMsg[errno] ?: defValue

    fun <T> request(key: Any, request: Request, type: TypeReference<T>, lifecycle: Lifecycle? = null,
                    onResult: ((errno: Int, msg: String, data: Any?) -> Unit)? = null): Int {
        if (isRunning(key)) {
            onResult?.invoke(CODE_MULTIPLE_REQUEST, getMsg(CODE_MULTIPLE_REQUEST), null)
            return -1
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
        return asyncStart(key, { Worker(key, request, type) }, lifecycle, onResult)
    }

    inline fun <reified T> inlineRequest(key: Any, request: Request, lifecycle: Lifecycle? = null,
                                         noinline onResponse: ((data: T) -> Unit)? = null,
                                         noinline onFailure: ((errno: Int, msg: String) -> Unit)? = null) =
            request(key, request, object : TypeReference<T>() {}, lifecycle) { errno, msg, data ->
                if (errno == CODE_OK) {
                    if (data is T)
                        onResponse?.invoke(data)
                    else
                        onFailure?.invoke(CODE_UNEXPECTED_RESPONSE, getMsg(CODE_UNEXPECTED_RESPONSE))
                } else {
                    onFailure?.invoke(errno, msg)
                }
            }

    inline fun <reified T> post(url: String, key: Any, params: JSONObject = defaultParams, lifecycle: Lifecycle? = null, postType: PostType = PostType.FORM,
                                noinline onResponse: ((data: T) -> Unit)? = null,
                                noinline onFailure: ((errno: Int, msg: String) -> Unit)? = null): Int {
        if (params != defaultParams) {
            for (k in defaultParams.keys()) {
                if (!params.has(k)) params.put(k, defaultParams.get(k))
            }
        }
        return inlineRequest(key, Request.Builder().also { builder ->
            builder.url(url)
            if (debugMode) {
                Log.d(TAG, "$url request:")
            }
            if (params.length() > 0) {
                when (postType) {
                    PostType.FORM -> builder.post(RequestBody.create(DATA_TYPE_FORM, formatJsonToForm(params)))
                    PostType.JSON -> builder.post(RequestBody.create(DATA_TYPE_JSON, params.toString()))
                }
                if (debugMode) {
                    formatJsonString(params.toString()).split('\n').forEach { Log.d(TAG, it) }
                }
            }
        }.build(), lifecycle, onResponse, onFailure)
    }

    suspend inline fun <reified T> postCoroutine(url: String, key: Any, params: JSONObject = defaultParams, lifecycle: Lifecycle? = null, postType: PostType = PostType.FORM,
                                                 noinline onFailure: ((errno: Int, msg: String) -> Unit)? = null): T {
        var cancelRequest = false
        return try {
            suspendCancellableCoroutine { continuation ->
                cancelRequest = true
                post<T>(url, key, params, lifecycle, postType, onResponse = { cancelRequest = false;continuation.resume(it) },
                        onFailure = { errno, msg -> cancelRequest = false;continuation.resumeWithException(InternalCancellationException(errno, msg)) })
            }
        } catch (e: CancellationException) {
            if (cancelRequest) cancel(key)
            if (e is InternalCancellationException)
                onFailure?.invoke(e.errno, e.msg)
            else
                onFailure?.invoke(CODE_CANCELED, getMsg(CODE_CANCELED))
            throw e
        }
    }

    inline fun <reified T> postFile(url: String, key: Any, lifecycle: Lifecycle? = null, file: File,
                                    noinline onResponse: ((data: T) -> Unit)? = null,
                                    noinline onFailure: ((errno: Int, msg: String) -> Unit)? = null) =
            inlineRequest(key, Request.Builder().url(url).post(RequestBody.create(DATA_TYPE_FILE, file)).build(), lifecycle, onResponse, onFailure)


    inline fun <reified T> postMultipartForm(url: String, key: Any, lifecycle: Lifecycle? = null, files: List<Pair<String, File>> = emptyList(),
                                             noinline onParams: ((params: JSONObject) -> JSONObject)? = null,
                                             noinline onResponse: ((data: T) -> Unit)? = null,
                                             noinline onFailure: ((errno: Int, msg: String) -> Unit)? = null): Int {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        for ((name, file) in files) {
            builder.addFormDataPart(name, file.name, RequestBody.create(DATA_TYPE_FILE, file))
        }
        onParams?.invoke(JSONObject())?.let { params ->
            for (k in params.keys()) {
                builder.addFormDataPart(k, params.getString(k))
            }
        }
        return inlineRequest(key, Request.Builder().url(url).post(builder.build()).build(), lifecycle, onResponse, onFailure)
    }

    private inner class Worker<T>(key: Any, private val request: Request, private val type: TypeReference<T>,
                                  private val wrap: Boolean = true) : Task(key) {
        override fun onCancel() {
            observers.forEach { it?.invoke(CODE_CANCELED, getMsg(CODE_CANCELED), null) }
        }

        override fun onObserverUnregistered(observer: ((errno: Int, msg: String, data: Any?) -> Unit)?) {
            observer?.invoke(CODE_CANCELED, getMsg(CODE_CANCELED), null)
        }

        override fun doInBackground() {
            val call = okHttpClient.newCall(request)
            val (errno, data) = run {
                val raw = try {
                    call.execute()
                } catch (e: IOException) {
                    Log.e(TAG, "execute IOException: ${e.message}")
                    return@run (if (isNetworkAvailable) CODE_TIMEOUT else CODE_NO_AVAILABLE_NETWORK) to null
                }.use { response ->
                    if (!response.isSuccessful) {
                        Log.e(TAG, "HTTP code ${response.code()}")
                        return@run CODE_UNEXPECTED_RESPONSE to null
                    }
                    return@use try {
                        response.body()!!.string()
                    } catch (e: IOException) {
                        Log.e(TAG, "read string IOException: ${e.message}")
                        return@run (if (isNetworkAvailable) CODE_TIMEOUT else CODE_NO_AVAILABLE_NETWORK) to null
                    }
                }
                try {
                    return@run CODE_OK to mapper.readValue<T>(raw, type).also {
                        if (debugMode) {
                            Log.d(TAG, "${request.url()} response:")
                            formatJsonString(raw).split('\n').forEach { Log.d(TAG, it) }
                        }
                    }
                } catch (e: Throwable) {
                    Log.e(TAG, "readValueException: ${e.message}")
                    if (debugMode) {
                        Log.e(TAG, "${request.url()} raw response:\n$raw")
                    }
                    return@run CODE_UNEXPECTED_RESPONSE to null
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