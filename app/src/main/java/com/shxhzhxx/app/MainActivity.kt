package com.shxhzhxx.app

import android.os.Build
import android.os.Bundle
import android.util.Log
import com.shxhzhxx.sdk.activity.DownloadActivity
import com.shxhzhxx.sdk.activity.cropPictureCoroutine
import com.shxhzhxx.sdk.activity.fullscreen
import com.shxhzhxx.sdk.activity.takePictureCoroutine
import com.shxhzhxx.sdk.imageLoader
import com.shxhzhxx.sdk.net
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
            launch {
                imageLoader.load(iv, cropPictureCoroutine(takePictureCoroutine().uri, 1f, 1f).uri.toString())
            }
            net.postForm<Response<List<Model>>>("https://wanandroid.com/wxarticle/chapters/json", "config", lifecycle,
                    onParams = { it.put("a", 1).put("b", 2) },
                    onResult = { errno, msg, data ->
                        Log.d(TAG, "errno:$errno")
                        Log.d(TAG, "msg:$msg")
                        Log.d(TAG, "data:$data")
                        data?.data?.forEach { Log.d(TAG, it.name) }
                    }
            )
        }
    }
}

data class Response<T>(val data: T)
data class Model(val name: String)
