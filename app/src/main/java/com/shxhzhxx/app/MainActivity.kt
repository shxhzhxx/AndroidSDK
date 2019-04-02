package com.shxhzhxx.app

import android.os.Build
import android.os.Bundle
import android.util.Log
import com.shxhzhxx.sdk.activity.DownloadActivity
import com.shxhzhxx.sdk.activity.cropPictureCoroutine
import com.shxhzhxx.sdk.activity.fullscreen
import com.shxhzhxx.sdk.activity.takePictureCoroutine
import com.shxhzhxx.sdk.imageLoader
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.delay
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
            val job = launch {
                imageLoader.load(iv, cropPictureCoroutine(takePictureCoroutine().uri, 1f, 1f).file.absolutePath)
            }
            launch {
                delay(2000)
                job.cancel()
                Log.d(TAG, "cancel")
            }
        }
    }
}
