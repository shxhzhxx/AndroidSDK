package com.shxhzhxx.app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.shxhzhxx.sdk.imageLoader
import com.shxhzhxx.sdk.net
import com.shxhzhxx.sdk.network.formatJsonString
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val onResult: (Int, String, String?) -> Unit = { errno, msg, data ->
            Log.d(TAG, "errno:$errno")
            Log.d(TAG, "msg:$msg")
            if (data != null)
                Log.d(TAG, formatJsonString(data))
            else
                Log.d(TAG, "data:$data")
        }

        load.setOnClickListener {
            //            imageLoader.load(iv, "https://static.usasishu.com/image/2018/09/30/bg-index.jpg")
            imageLoader.bitmapLoader.urlLoader.asyncLoad("https://static.usasishu.com/image/2018/09/30/bg-index.jpg", onComplete = {
                net.postMultipartForm("https://www.baidu.com/", "upload", lifecycle, listOf("file" to it), onParams = { params ->
                    params.put("a", 1).put("b", 2)
                }, onResult = onResult)
            })
        }
    }
}
