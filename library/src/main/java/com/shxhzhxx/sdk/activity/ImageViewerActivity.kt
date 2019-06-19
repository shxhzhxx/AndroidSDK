package com.shxhzhxx.sdk.activity


import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.github.chrisbanes.photoview.PhotoView
import com.shxhzhxx.sdk.R
import com.shxhzhxx.sdk.imageLoader
import com.shxhzhxx.sdk.utils.copyTo
import com.shxhzhxx.sdk.utils.launchFinally
import com.shxhzhxx.sdk.utils.loadCoroutine
import com.shxhzhxx.sdk.utils.toast
import kotlinx.android.synthetic.main.activity_image_viewer.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "ImageViewerActivity"

/**
 * [com.shxhzhxx.sdk.ui.HolderRefAdapter] could help.
 */

fun FragmentActivity.launchImageViewerActivity(paths: List<String>, position: Int, pairs: List<Pair<View, String>>?) {
    val intent = Intent(this, ImageViewerActivity::class.java)
    intent.putStringArrayListExtra("paths", ArrayList(paths))
    intent.putExtra("position", position)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && pairs != null) {
        intent.putExtra("transition", true)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this,
                * pairs.map { android.util.Pair<View, String>(it.first, it.second) }.toTypedArray()).toBundle())
    } else {
        intent.putExtra("transition", false)
        startActivity(intent)
    }
}

class ImageViewerActivity : ForResultActivity(), View.OnClickListener {
    private val paths by lazy { intent.getStringArrayListExtra("paths") }
    private val cache = Stack<PhotoView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fullscreen(hideStatusBar = false, lightStatusBar = false)
        }
        setContentView(R.layout.activity_image_viewer)

        val position = intent.getIntExtra("position", -1)
        if (paths == null || position >= paths.size || position < 0) {
            Log.e(TAG, "Invalid params. paths:$paths   position:$position")
            return
        }

        indicator.visibility = if (paths.size < 2) View.INVISIBLE else View.VISIBLE
        var transition = intent.getBooleanExtra("transition", false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && transition) {
            supportPostponeEnterTransition()
            pager.transitionName = paths[position]
        }
        share.setOnClickListener {
            launch {
                val file = paths[pager.currentItem].toFile()
                startActivity(Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, exposeUriForFile(file))
                    type = "image/*"
                }, getString(R.string.share_picture)))
            }
        }
        download.setOnClickListener {
            requestPermissions(listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), onGrant = {
                launch { paths[pager.currentItem].toFile().saveImage() }
            })
        }
        pager.offscreenPageLimit = 2
        pager.adapter = object : PagerAdapter() {
            override fun getCount() = paths.size

            override fun isViewFromObject(view: View, obj: Any) = view === obj

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val view = try {
                    cache.pop()
                } catch (e: EmptyStackException) {
                    PhotoView(this@ImageViewerActivity).also { v ->
                        v.setOnClickListener(this@ImageViewerActivity)
                    }
                }

                val path = paths[position]
                if (transition) {
                    transition = false
                    launchFinally({ supportStartPostponedEnterTransition() }) {
                        imageLoader.loadCoroutine(view, path, centerCrop = false)
                    }
                } else {
                    imageLoader.load(view, path, centerCrop = false)
                }
                container.addView(view)
                return view
            }

            override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
                container.removeView(obj as PhotoView)
                cache.add(obj)
            }
        }
        val listener = object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    pager.transitionName = paths[position]
                }
                @SuppressLint("SetTextI18n")
                indicator.text = "${position + 1}/${paths.size}"
            }
        }
        pager.addOnPageChangeListener(listener)
        pager.currentItem = position
        listener.onPageSelected(position)
    }

    override fun onClick(v: View) {
        onBackPressed()
    }

    private suspend fun String.toFile(): File {
        val file = File(this)
        if (file.exists())
            return file
        return imageLoader.bitmapLoader.urlLoader.loadCoroutine(this)
    }

    private suspend fun File.saveImage() {
        convert {
            val dst = createPictureFile()
                    ?: run { runOnUiThread { toast(getString(R.string.unavailable_external_storage)) };return@convert }
            if (this copyTo dst) {
                runOnUiThread {
                    mediaScanFile(dst)
                    toast(getString(R.string.picture_saved_format, dst.absolutePath))
                }
            }
        }
    }
}
