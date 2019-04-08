package com.shxhzhxx.app

import android.os.Build
import android.os.Bundle
import com.shxhzhxx.sdk.activity.DownloadActivity
import com.shxhzhxx.sdk.activity.fullscreen
import com.shxhzhxx.sdk.utils.Param
import kotlinx.android.synthetic.main.activity_main.*

const val TAG = "MainActivity"

const val url = "http://download.alicdn.com/wireless/dingtalk/latest/rimet_700219.apk"
const val url1 = "http://10.63.3.90:8887/apk/sdk.apk"
const val url2 = "https://static.usasishu.com/notebook.apk"

const val api = "https://member.uuabc.com/api/open/serviceConfig.php?act=getConfigData"
const val api1 = "https://wanandroid.com/wxarticle/chapters/json"
const val api2 = "https://static.usasishu.com/api.txt"
const val api3 = "https://static.usasishu.com/null.txt"
const val api4 = "https://static.usasishu.com/empty.txt"

class MainActivity : DownloadActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fullscreen()
        }

        video.setDataSource("https://file.yizhujiao.com/11%20%E5%88%87%E5%9D%97%E6%8B%BC%E6%8E%A5%E6%B3%95.mp4")
//        video.start()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
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
