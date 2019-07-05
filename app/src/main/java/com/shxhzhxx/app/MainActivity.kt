package com.shxhzhxx.app

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.shxhzhxx.imageloader.ROUND_CIRCLE
import com.shxhzhxx.sdk.activity.DownloadActivity
import com.shxhzhxx.sdk.activity.setStatusBarColor
import com.shxhzhxx.sdk.imageLoader
import com.shxhzhxx.sdk.net
import com.shxhzhxx.sdk.network.CODE_NO_AVAILABLE_NETWORK
import com.shxhzhxx.sdk.network.CODE_TIMEOUT
import com.shxhzhxx.sdk.utils.Param
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


const val TAG = "MainActivity"


const val u1 = "https://static.usasishu.com/image/2018/09/30/bg-index.jpg"
const val u2 = "https://static.usasishu.com/image/2018/09/30/bg-china-map.png"
const val u3 = "https://static.usasishu.com/image/2018/10/12/how_to_learn_banner.png"
const val u4 = "https://static.usasishu.com/image/2018/10/12/time_plan_bg.png"
const val u5 = "https://static.usasishu.com/image/2018/10/15/0001.png"

const val url = "http://download.alicdn.com/wireless/dingtalk/latest/rimet_700219.apk"
const val url1 = "http://10.63.3.90:8887/apk/sdk.apk"
const val url2 = "https://static.usasishu.com/notebook.apk"
const val url3 = "https://file.yizhujiao.com/11%20%E5%88%87%E5%9D%97%E6%8B%BC%E6%8E%A5%E6%B3%95.mp4"

const val api = "https://member.uuabc.com/api/open/serviceConfig.php?act=getConfigData"
const val api1 = "https://wanandroid.com/wxarticle/chapters/json"
const val api2 = "https://static.usasishu.com/api.txt"
const val api3 = "https://static.usasishu.com/null.txt"
const val api4 = "https://static.usasishu.com/empty.txt"
const val apiJson = "https://image.yizhujiao.com/testApi2.txt"
const val apiStringArr = "https://image.yizhujiao.com/stringArrApi.txt"
const val apiFailure = "https://static.usasishu.com/failureApi.txt"
const val apiFailure2 = "https://static.usasishu.com/failureApi2.txt"
const val apiFailure3 = "https://static.usasishu.com/failureApi3.txt"
const val apiFailure5 = "https://static.usasishu.com/failureApi5.txt"
const val apiFailure6 = "https://static.usasishu.com/failureApi6.txt"
const val config = "https://static.usasishu.com/config.txt"
const val testApi3 = "https://static.usasishu.com/testApi3.txt"
const val emptySuccess = "https://static.usasishu.com/emptySuccess.txt"
const val debugApi = "https://static.usasishu.com/debugApi.txt"
const val debugApi2 = "https://static.usasishu.com/debugApi2.txt"
const val debugApi3 = "https://static.usasishu.com/debugApi3.txt"
const val string = "https://static.usasishu.com/string.txt"
const val emptyStrSuccess = "https://static.usasishu.com/emptyStrSuccess.txt"
const val int = "https://static.usasishu.com/int.txt"
const val int2 = "https://static.usasishu.com/int2.txt"
const val bool = "https://static.usasishu.com/bool.txt"
const val bool2 = "https://static.usasishu.com/bool2.txt"
const val float = "https://static.usasishu.com/float.txt"
const val float2 = "https://static.usasishu.com/float2.txt"
const val debugCodeApi = "https://static.usasishu.com/debugCodeApi6.txt"

class MainActivity : DownloadActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "onCreate")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setStatusBarColor(Color.WHITE)
        }

        object : CoroutineScope {
            override val coroutineContext: CoroutineContext
                get() = Dispatchers.Main + SupervisorJob()
        }.launch {
            val r: suspend (Int) -> Unit = {
            }
            val config = net.postCoroutine<Debug3>(debugApi3, lifecycle = lifecycle, retryList = listOf(
                    listOf(CODE_NO_AVAILABLE_NETWORK, CODE_TIMEOUT) to { errno -> net.requireNetwork() },
                    listOf(3005) to r
            ))
            Log.d(TAG, "config:$config")
        }

        imageLoader.load(iv, "http://p15.qhimg.com/bdm/720_444_0/t01b12dfd7f42342197.jpg", centerCrop = false, roundingRadius = ROUND_CIRCLE)
//        Glide.with(this).load("http://p15.qhimg.com/bdm/720_444_0/t01b12dfd7f42342197.jpg").apply(RequestOptions.bitmapTransform(RoundedCorners(40))).into(iv)
//        RoundedCornersTransformation()

        iv.setOnClickListener {
            Log.d(TAG, "state:${lifecycle.currentState.name}")
            lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_START)
                fun onStart() {
                    Log.d(TAG, "ON_START")
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
                fun onResume() {
                    Log.d(TAG, "ON_RESUME")
                }
            })
            Log.d(TAG,"do something here")
        }
    }
}


data class CodeModel(val sisValids: Boolean)
class ConfigEx(val hx_contact_group_for_teacher: String) : Config(hx_contact_group_for_teacher)
open class Config(val serviceIMNumber: String)

data class User(val name: String?, val age: Int?)
data class Debug2(val list: List<User>?)
data class Debug3(val list: List<User>)
data class Debug(val list: String?)
data class Student(
        val name: String,
        val courseId: Int,
        val id: Int,
        val order: Int,
        val parentChapterId: Int,
        val visible: Int,
        val userControlSetTop: Boolean,
        val children: List<String>
)


data class Nest(
        val a: Int,
        val b: Int,
        val c: Int
)

data class Model(val name: String)
class Empty

val aaa = Param("aaa", "")
val bbb = Param("bbb", 0L)
val ccc = Param("ccc", 0)
val ddd = Param("ddd", true)
val eee = Param("eee", 0f)
val invalid = Param("eee", Model("aaa"))
