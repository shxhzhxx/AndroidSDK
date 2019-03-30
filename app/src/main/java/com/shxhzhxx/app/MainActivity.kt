package com.shxhzhxx.app

import android.os.Bundle
import com.shxhzhxx.sdk.activity.*
import com.shxhzhxx.sdk.imageLoader
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

class MainActivity : ForResultActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        load.setOnClickListener {
            launch {
                imageLoader.load(iv, centerCrop = false, path = cropPictureCoroutine(
                        takePictureCoroutine()?.uri, 1f, 1f)?.uri?.toString())
            }
        }
    }
}
