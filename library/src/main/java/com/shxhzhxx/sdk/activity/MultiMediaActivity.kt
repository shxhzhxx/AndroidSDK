package com.shxhzhxx.sdk.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.shxhzhxx.imageloader.BitmapLoader
import com.shxhzhxx.sdk.utils.FileUtils
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

abstract class MultiMediaActivity : ForResultActivity() {
    abstract class GetMediaListener {
        fun onSuccess(uri: Uri) {}

        fun onFailed() {}

        fun onRotate() {}
    }

    private fun loadRotateBitmap(file: File, width: Int, height: Int, degree: Int): Bitmap? {
        return null
//        val w: Int
//        val h: Int
//        when (degree) {
//            90, 270 -> {
//                w = height
//                h = width
//            }
//            180 -> {
//                w = width
//                h = height
//            }
//            else -> return BitmapLoader.loadBitmap(file, width, height)
//        }
//        val bitmap = BitmapLoader.loadBitmap(file, w, h) ?: return null
//        val matrix = Matrix()
//        matrix.postRotate(degree.toFloat())
//        return Bitmap.createBitmap(bitmap!!, 0, 0, bitmap!!.getWidth(), bitmap!!.getHeight(), matrix, true)
    }

    private fun readDegree(file: File?): Int {
        if (file == null || !file.exists())
            return -1
        try {
            val exifInterface = ExifInterface(file.absolutePath)
            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_NORMAL -> return 0
                ExifInterface.ORIENTATION_ROTATE_90 -> return 90
                ExifInterface.ORIENTATION_ROTATE_180 -> return 180
                ExifInterface.ORIENTATION_ROTATE_270 -> return 270
            }
        } catch (ignore: IOException) {
        }

        return -1
    }

    private fun reviseRotateImageFile(file: File, width: Int, height: Int) {
        val degree = readDegree(file)
        if (degree > 0) {
            val bitmap = loadRotateBitmap(file, width, height, degree) ?: return
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, FileOutputStream(file))
            } catch (ignore: FileNotFoundException) {
            }

        }
    }

    fun getPhotoByCamera(listener: GetMediaListener) {
        getPhotoByCamera(0, 0, listener)
    }

    fun getPhotoByCamera(width: Int, height: Int, listener: GetMediaListener?) {
        if (listener == null)
            return
        requestPermissions(listOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),onGranted = {
            val uri: Uri
            val file: File
            try {
                file = FileUtils.createExternalCacheFile("png")
                uri = FileUtils.getUriForFile(this@MultiMediaActivity, file)
            } catch (e: IOException) {
                listener.onFailed()
                return@requestPermissions
            }

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(Intent.createChooser(intent, null)){ resultCode, _ ->
                    if (resultCode == Activity.RESULT_OK) {
                        listener.onRotate()
                        Thread(Runnable {
                            reviseRotateImageFile(file, width, height)
                            runOnUiThread { listener.onSuccess(Uri.fromFile(file)) }
                        }).start()
                    } else {
                        listener.onFailed()
                    }
                }
            } else {
                listener.onFailed()
            }
        })
    }

    fun getPhotoByAlbum(listener: GetMediaListener) {
        pick("image/*", listener)
    }

    fun getVideo(listener: GetMediaListener) {
        pick("video/*", listener)
    }

    fun pick(type: String, listener: GetMediaListener?) {
        if (listener == null)
            return
        requestPermissions(listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), onGranted = {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = type
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(Intent.createChooser(intent, null)) { resultCode, data ->
                    if (resultCode == Activity.RESULT_OK) {
                        listener.onSuccess(data!!.data!!)
                    } else {
                        listener.onFailed()
                    }
                }
            } else {
                listener.onFailed()
            }
        })
    }

    fun cropPhoto(src: Uri?, aspectX: Int, aspectY: Int, listener: GetMediaListener?) {
        if (src == null || aspectX <= 0 || aspectY <= 0 || listener == null) {
            return
        }
        val out: Uri
        try {
            val file = FileUtils.createExternalCacheFile("png")
            out = Uri.fromFile(file)
        } catch (e: IOException) {
            listener.onFailed()
            return
        }

        val intent = UCrop.of(src, out).withAspectRatio(aspectX.toFloat(), aspectY.toFloat()).getIntent(this)

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent) { resultCode, _ ->
                if (resultCode == Activity.RESULT_OK) {
                    listener.onSuccess(out)
                } else {
                    listener.onFailed()
                }
            }
        } else {
            listener.onFailed()
        }
    }

    fun getFile(listener: GetMediaListener?) {
        if (listener == null)
            return
        requestPermissions(listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), onGranted = {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(Intent.createChooser(intent, null)) { resultCode, data ->
                    if (resultCode == Activity.RESULT_OK) {
                        listener.onSuccess(data!!.data!!)
                    } else {
                        listener.onFailed()
                    }
                }
            } else {
                listener.onFailed()
            }
        })
    }
}
