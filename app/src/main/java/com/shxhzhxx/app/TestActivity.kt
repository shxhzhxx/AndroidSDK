package com.shxhzhxx.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity

class TestActivity:AppCompatActivity(){
    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent().putExtra("a",1).setData(Uri.EMPTY))
        super.onBackPressed()
    }
}