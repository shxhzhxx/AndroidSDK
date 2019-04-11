package com.shxhzhxx.app

import android.os.Build
import android.os.Bundle
import com.shxhzhxx.sdk.activity.*
import com.shxhzhxx.sdk.imageLoader
import com.shxhzhxx.sdk.utils.Param
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch

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

class MainActivity : DownloadActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fullscreen()
        }

        imageLoader.bitmapLoader.urlLoader.load(u2, onLoad = {
            val paths = listOf(u1, it.absolutePath, u3, u4, u5)
            val ivs = listOf(iv1, iv2, iv3, iv4, iv5)

            val pairs = ivs.mapIndexed { index, imageView -> imageView to paths[index] }

            for ((iv, path) in pairs) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    iv.transitionName = path
                }
                imageLoader.load(iv, path, centerCrop = false)
                iv.setOnClickListener {
                    launchImageViewerActivity(paths, ivs.indexOf(iv), pairs)
                }
            }
        })
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
