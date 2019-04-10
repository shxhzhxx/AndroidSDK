package com.shxhzhxx.sdk.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.shxhzhxx.sdk.R
import com.shxhzhxx.sdk.utils.statusBarHeight
import kotlinx.android.synthetic.main.activity_video_viewer.*
import java.io.FileNotFoundException

fun FragmentActivity.launchVideoViewerActivity(path: String) {
    startActivity(Intent(this, VideoViewerActivity::class.java).putExtra("path", path))
}

class VideoViewerActivity : ForResultActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fullscreen()
        }
        setContentView(R.layout.activity_video_viewer)

        back.layoutParams = (back.layoutParams as FrameLayout.LayoutParams).apply {
            topMargin = statusBarHeight
            leftMargin = statusBarHeight
        }
        back.setOnClickListener { onBackPressed() }

        video.getPreview().setImageDrawable(ColorDrawable(Color.BLACK))
        val path = intent.getStringExtra("path")
        try {
            //check whether path is valid uri
            val uri = Uri.parse(path)
            (contentResolver.openInputStream(uri)
                    ?: throw FileNotFoundException("Unable to create stream")).close()

            video.setDataSource(uri)
        } catch (e: Throwable) {
            video.setDataSource(path)
        }
        video.start()
    }
}
