package com.shxhzhxx.app

import android.app.Activity
import android.os.Bundle
import com.shxhzhxx.sdk.activity.DownloadActivity

class TestActivity :DownloadActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_OK)
    }
}