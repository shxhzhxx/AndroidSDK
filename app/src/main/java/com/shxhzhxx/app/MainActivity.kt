package com.shxhzhxx.app

import android.os.Build
import android.os.Bundle
import android.util.Log
import com.shxhzhxx.sdk.activity.DownloadActivity
import com.shxhzhxx.sdk.activity.fullscreen
import com.shxhzhxx.sdk.activity.postCoroutine
import com.shxhzhxx.sdk.utils.Param
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch

const val TAG = "MainActivity"

class MainActivity : DownloadActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fullscreen()
        }
        iv.setOnClickListener {
            val url = "http://download.alicdn.com/wireless/dingtalk/latest/rimet_700219.apk"
            val url1 = "http://10.63.3.90:8887/apk/sdk.apk"
            val url2 = "https://static.usasishu.com/notebook.apk"
//            launch {
//                imageLoader.load(iv, cropPictureCoroutine(takePictureCoroutine().uri, 1f, 1f).uri.toString())
//            }

            val api = "https://member.uuabc.com/api/open/serviceConfig.php?act=getConfigData"
            val api1 = "https://wanandroid.com/wxarticle/chapters/json"
            val api2 = "https://static.usasishu.com/api.txt"
            val api3 = "https://static.usasishu.com/null.txt"
            val api4 = "https://static.usasishu.com/empty.txt"

            launch {
                Log.d(TAG, "launch")
                val config = postCoroutine<Config>(api)
                Log.d(TAG, "launch success: $config")
            }
        }
    }
}


data class Config(
        val serviceIMNumber: String?,
        val xh_config_id: String?,
        val hx_contact_group: String?,
        val service_IM_number_for_teacher: String?,
        val xh_config_id_for_teacher: String?,
        val hx_contact_group_for_teacher: String?
)

data class Model(val name: String?)
class Empty

val aaa = Param("aaa", "")
val bbb = Param("bbb", 0L)
val ccc = Param("ccc", 0)
val ddd = Param("ddd", true)
val eee = Param("eee", 0f)
val invalid = Param("eee", Model("aaa"))
