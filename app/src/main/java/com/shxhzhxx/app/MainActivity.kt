package com.shxhzhxx.app

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shxhzhxx.sdk.activity.DownloadActivity
import com.shxhzhxx.sdk.activity.fullscreen
import com.shxhzhxx.sdk.activity.setStatusBarColor
import com.shxhzhxx.sdk.imageLoader
import com.shxhzhxx.sdk.net
import com.shxhzhxx.sdk.network.CODE_NO_AVAILABLE_NETWORK
import com.shxhzhxx.sdk.network.CODE_TIMEOUT
import com.shxhzhxx.sdk.ui.ListFragment
import com.shxhzhxx.sdk.utils.Param
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
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
        val json = Json(JsonConfiguration.Stable.copy(strictMode = false))

        object : CoroutineScope {
            override val coroutineContext: CoroutineContext
                get() = Dispatchers.Main + SupervisorJob()
        }.launch {
            val r: suspend (Int) -> Unit = {
            }
            val raw = net.postCoroutine<String>(api, lifecycle = lifecycle, retryList = listOf(
                    listOf(CODE_NO_AVAILABLE_NETWORK, CODE_TIMEOUT) to { errno -> net.requireNetwork() },
                    listOf(3005) to r
            ))
            Log.d(TAG, "raw:$raw")
            val config = json.parse(Config.serializer(),raw)
            Log.d(TAG, "config:$config")
            Log.d(TAG, "test:${config.test}")
        }

//        imageLoader.load(iv, "http://p15.qhimg.com/bdm/720_444_0/t01b12dfd7f42342197.jpg", centerCrop = false, roundingRadius = ROUND_CIRCLE)


    }
}


data class CodeModel(val sisValids: Boolean)

@Serializable
data class Config(val serviceIMNumber: String){
    val test get() = serviceIMNumber.takeLast(10)
}

data class User(val name: String?, val age: Int?)

data class Debug2(val list: List<User>?)

data class Debug3<T>(val list: List<T>)

@Serializable
data class Debug4<T, V>(val list1: List<T>, val list2: List<V>)

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


class MainFragment : ListFragment<Unit, RecyclerView.ViewHolder, MainFragment.MainAdapter>() {
    inner class MainAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                object : RecyclerView.ViewHolder(TextView(parent.context).also { it.setPadding(200, 200, 200, 200) }) {}

        override fun getItemCount() = listSize

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder.itemView as TextView).text = "item$position"
        }
    }

    override fun onAdapter() = MainAdapter()

    override fun onNextPage(page: Int, onResult: () -> Unit, onLoad: (list: List<Unit>) -> Unit) {
        launch {
            delay(1000)
            onResult()
//            delay(20)
            if (listSize > 30) {
                mutableListOf<Unit>().also { list -> repeat(7) { list.add(Unit) } }.apply(onLoad)
            } else {
                mutableListOf<Unit>().also { list -> repeat(pageSize()) { list.add(Unit) } }.apply(onLoad)
            }
        }
    }
}
