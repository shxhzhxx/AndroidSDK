package com.shxhzhxx.app

import android.Manifest
import android.os.Bundle
import com.shxhzhxx.sdk.activity.PermissionRequestActivity
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"

class MainActivity : PermissionRequestActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        load.setOnClickListener {
            performRequestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), onPermissionGranted = {

            }, onPermissionDenied = {

            })
        }
    }
}
