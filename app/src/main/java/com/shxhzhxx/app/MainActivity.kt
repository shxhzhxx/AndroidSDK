package com.shxhzhxx.app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.shxhzhxx.sdk.network.formatJsonString
import com.shxhzhxx.sdk.utils.ConditionalAction
import com.shxhzhxx.sdk.utils.toast
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
            toast(listOf("测试", "shxhzhxx", "djlsafjdks", "hahahah").random())
        }
        val conRun = ConditionalAction(2) {

        }
        conRun[1]=true
    }
}
