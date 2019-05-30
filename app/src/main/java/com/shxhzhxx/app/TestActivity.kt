package com.shxhzhxx.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.shxhzhxx.sdk.activity.DownloadActivity

class TestActivity :DownloadActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TestActivity","onCreate")
        setResult(Activity.RESULT_OK)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("TestActivity","onNewIntent")
    }
}