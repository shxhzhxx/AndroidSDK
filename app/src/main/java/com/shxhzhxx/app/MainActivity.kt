package com.shxhzhxx.app

import android.os.Bundle
import com.shxhzhxx.sdk.activity.DownloadActivity
import com.shxhzhxx.sdk.activity.promptInstall
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

class MainActivity : DownloadActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        iv.setOnClickListener {
            val url = "http://download.alicdn.com/wireless/dingtalk/latest/rimet_700219.apk"
            val url1 = "http://10.63.3.90:8887/apk/sdk.apk"
            val url2 = "https://static.usasishu.com/notebook.apk"
            launch {

                val uri = downloadCoroutine(url2)
                if (uri != null)
                    promptInstall(uri)
            }
        }
    }
}
