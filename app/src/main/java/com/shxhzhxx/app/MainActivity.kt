package com.shxhzhxx.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.blankj.utilcode.util.UriUtils
import com.shxhzhxx.sdk.activity.CoroutineActivity
import com.shxhzhxx.sdk.imageLoader
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

private const val TAG = "MainActivity"

class MainActivity : CoroutineActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        load.setOnClickListener {
            launch {
                Log.d(TAG, "launch start")

                requestPermissionsCoroutine(listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                Log.d(TAG, "granted")

                val (resultCode, data) = startActivityForResultCoroutine(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                })
                Log.d(TAG, "resultCode:$resultCode    data:$data")

                val uri = data?.data ?: return@launch

                contentResolver.openInputStream(Uri.parse(uri.path))
                val file = UriUtils.uri2File(Uri.parse(uri.toString()))
                Log.d(TAG, "degree:${readDegree(file)}")
                imageLoader.load(iv, file.absolutePath)

                Log.d(TAG, "launch end")
            }
        }
    }

    private fun readDegree(file: File?): Int {
        if (file == null || !file.exists()) {
            Log.d(TAG, "aaaaaaaaaaaaaaa")
            return -1
        }
        try {
            val exifInterface = ExifInterface(file.absolutePath)
            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            Log.d(TAG,"orientation:$orientation")
            return when (orientation) {
                ExifInterface.ORIENTATION_NORMAL -> 0
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> -1
            }
        } catch (ignore: IOException) {
            Log.d(TAG, "bbbbbbbbbbbbbbbbb")
            ignore.printStackTrace()
        }

        return -1
    }
}
