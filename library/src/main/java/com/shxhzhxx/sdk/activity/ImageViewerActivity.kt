package com.shxhzhxx.sdk.activity


import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.github.chrisbanes.photoview.PhotoView
import com.shxhzhxx.sdk.R
import com.shxhzhxx.sdk.imageLoader
import com.shxhzhxx.sdk.utils.copyTo
import com.shxhzhxx.sdk.utils.toast
import kotlinx.android.synthetic.main.activity_image_viewer.*
import java.io.File
import java.util.*

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

class ImageViewerActivity : ForResultActivity(), View.OnClickListener, View.OnLongClickListener {
    private val paths by lazy { intent.getStringArrayListExtra("paths") }
    private val cache = Stack<PhotoView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fullscreen(lightStatusBar = true)
        }
        setContentView(R.layout.activity_image_viewer)

        val position = intent.getIntExtra("position", -1)
        if (paths == null || position >= paths.size || position < 0) {
            Log.e(TAG, "Invalid params. paths:$paths   position:$position")
            return
        }

        var transition = intent.getBooleanExtra("transition", false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && transition) {
            supportPostponeEnterTransition()
            pager.transitionName = paths[position]
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
                        v.setOnLongClickListener(this@ImageViewerActivity)
                    }
                }

                val path = paths[position]
                if (transition) {
                    transition = false
                    imageLoader.load(view, path, centerCrop = false,
                            onLoad = { supportStartPostponedEnterTransition() },
                            onFailure = { supportStartPostponedEnterTransition() },
                            onCancel = { supportStartPostponedEnterTransition() })
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
        pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    pager.transitionName = paths[position]
                }
            }
        })
        pager.currentItem = position
    }

    override fun onClick(v: View) {
        onBackPressed()
    }

    override fun onLongClick(v: View): Boolean {
        AlertDialog.Builder(v.context, R.style.AutoSizeAlertDialog).setItems(arrayOf(getString(R.string.save_image_to_sdcard))) { _, _ ->
            requestPermissions(listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), onGrant = {
                val path = paths[pager.currentItem]
                val f = File(path)
                if (f.exists()) {
                    saveImage(f)
                } else {
                    imageLoader.bitmapLoader.urlLoader.load(path, onLoad = { saveImage(it) })
                }
            })
        }.create().show()
        return true
    }

    private fun saveImage(file: File) {
        val dst = createPictureFile()
                ?: run { toast(getString(R.string.unavailable_external_storage));return }
        if (file copyTo dst) {
            mediaScanFile(dst)
            toast(getString(R.string.image_saved_format, dst.absolutePath))
        }
    }
}
