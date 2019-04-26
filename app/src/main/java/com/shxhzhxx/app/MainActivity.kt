package com.shxhzhxx.app

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.shxhzhxx.sdk.activity.DownloadActivity
import com.shxhzhxx.sdk.activity.launchImageViewerActivity
import com.shxhzhxx.sdk.activity.setStatusBarColor
import com.shxhzhxx.sdk.imageLoader
import com.shxhzhxx.sdk.network.mapper
import com.shxhzhxx.sdk.utils.Param
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

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
const val config = "https://static.usasishu.com/config.txt"
const val testApi3 = "https://static.usasishu.com/testApi3.txt"
const val emptySuccess = "https://static.usasishu.com/emptySuccess.txt"
const val debugApi = "https://static.usasishu.com/debugApi.txt"
const val debugApi2 = "https://static.usasishu.com/debugApi2.txt"

class MainActivity : DownloadActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setStatusBarColor(Color.WHITE)
        }

        val ivs = listOf(iv1, iv2, iv3, iv4, iv5)
        val paths = listOf(u1, u2, u3, u4, u5)
        val pairs = ivs.mapIndexed { index, imageView -> imageView to paths[index] }
        ivs.forEachIndexed { index, imageView ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView.transitionName = paths[index]
            }
            imageLoader.load(imageView, paths[index], centerCrop = false)
            imageView.setOnClickListener {
                launchImageViewerActivity(paths, index, pairs)
            }
        }
        launch {
            delay(1000)
//            val debug = postCoroutine<String>(debugApi2)
//            Log.d(TAG,"list:${debug}")
            main()
        }
    }


    private fun main() {
        val raw1 = JSONObject()
                .put("a", JSONObject()
                        .put("a", JSONObject().put("int",98))
                        .put("b", JSONObject()
                                .put("a", JSONObject().put("int",100))
                                .put("b", JSONObject().put("int",99))
                        ))
                .put("b", JSONObject()
                        .put("text", "b-branch"))
                .toString()
        Log.d(TAG, "raw1:$raw1")
        val ret1 = wwwrapResolve<IntNode>(raw1).first
        Log.d(TAG, "ret1:$ret1")
    }
}


data class Config(
        val serviceIMNumber: String,
        val xh_config_id: String,
        val hx_contact_group: String,
        val service_IM_number_for_teacher: String,
        val xh_config_id_for_teacher: String,
        val hx_contact_group_for_teacher: String
)

data class WrapNode(val text: String)
data class IntNode(val int:Int)

data class Wrap2<T,V>(val a: T, val b: V)
data class Wrap<T, V>(val a: T, val b: V)


inline fun <reified T> wwwrapResolve(raw: String) =
        wwrapResolve<Wrap<T, Wrap<T, T>>, WrapNode>(raw)

inline fun <reified T, reified V> wwrapResolve(raw: String) =
        wrapResolve<Wrap2<T, V>, V>(raw)

inline fun <reified T, reified V> wrapResolve(raw: String) =
        resolve<Wrap<T, V>>(raw, TypeFactory.defaultInstance().constructParametricType(Wrap::class.java, T::class.java, V::class.java)).let { it.a to it.b }

inline fun <reified T> resolve(raw: String, type: JavaType) =
        mapper.readValue<T>(raw, type)


//inline fun <reified T> wwrapResolve(raw: String) = wrapResolve<Wrap<T>>(raw, TypeFactory.defaultInstance().constructParametricType(Wrap::class.java, T::class.java)).data
//inline fun <reified T> wrapResolve(raw: String, type: JavaType) = resolve<Wrap<T>>(raw, TypeFactory.defaultInstance().constructParametricType(Wrap::class.java, type)).data
//inline fun <reified T> resolve(raw: String, type: JavaType): T {
//    return mapper.readValue<T>(raw, T::class.java.aaa())
//}

fun <T> Class<T>.aaa(): JavaType {
    val type = javaClass.kotlin
    return if (type.typeParameters.isNotEmpty()) {
        TypeFactory.defaultInstance().constructParametricType(this, *(type.typeParameters.map { it.javaClass.aaa() }.toTypedArray()))
    } else {
        TypeFactory.defaultInstance().constructType(this)
    }
}


data class User(val name: String?, val age: Int?)
data class Debug2(val list: List<User>?)
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

//data class Config(
//        val serviceIMNumber: String,
//        val xh_config_id: String,
//        val hx_contact_group: String,
//        val service_IM_number_for_teacher: String,
//        val xh_config_id_for_teacher: String,
//        val hx_contact_group_for_teacher: String,
//        val nest: Nest? = Nest(1, 1, 2),
//        val bool: Boolean?,
//
//        val hasService: Boolean = !serviceIMNumber.isBlank()
//)

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
