package com.shxhzhxx.sdk.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.shxhzhxx.sdk.imageLoader
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

enum class DocumentType(val value: String) {
    VIDEO("video/*"),
    IMAGE("image/*"),
    ALL("*/*")
}

data class MediaSource(val uri: Uri, val file: File)

infix fun Uri.to(that: File): MediaSource = MediaSource(this, that)

abstract class MultimediaActivity : ForResultActivity() {
    suspend fun openDocumentCoroutine(type: DocumentType = DocumentType.IMAGE): Uri? =
            suspendCoroutine { continuation -> openDocument(type, onOpened = { continuation.resume(it) }, onFailed = { continuation.resume(null) }) }

    fun openDocument(type: DocumentType = DocumentType.IMAGE, onOpened: (Uri) -> Unit, onFailed: (() -> Unit) = {}) {
        launch {
            if (!requestPermissionsCoroutine(listOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    } else {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }))) {
                onFailed()
                return@launch
            }

            val (resultCode, data) = startActivityForResultCoroutine(
                    Intent(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                                Intent.ACTION_OPEN_DOCUMENT
                            else
                                Intent.ACTION_GET_CONTENT
                    ).also { intent ->
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        intent.type = type.value
                        if (intent.resolveActivity(packageManager) == null) {
                            onFailed()
                            return@launch
                        }
                    })
            val uri = data?.data
            if (resultCode != Activity.RESULT_OK || uri == null) {
                onFailed()
            } else {
                onOpened(uri)
            }
        }
    }

    fun takePicture(onTaken: (Uri, File) -> Unit, onFailed: (() -> Unit) = {}) {
        launch {
            if (!requestPermissionsCoroutine(listOf(Manifest.permission.CAMERA))) {
                onFailed()
                return@launch
            }
            val file = createPictureFile() ?: run { onFailed(); return@launch }
            val uri = getUriForFile(file)
            val (resultCode, _) = startActivityForResultCoroutine(
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                        if (intent.resolveActivity(packageManager) == null) {
                            onFailed()
                            return@launch
                        }
                    })
            if (resultCode == Activity.RESULT_OK) {
                onTaken(uri, file)
            } else {
                onFailed()
            }
        }
    }

    suspend fun takePictureCoroutine(): MediaSource? =
            suspendCoroutine { continuation -> takePicture(onTaken = { uri, file -> continuation.resume(uri to file) }, onFailed = { continuation.resume(null) }) }

    fun Context.getUriForFile(file: File): Uri = FileProvider.getUriForFile(this, "$packageName.FileProvider", file)

    fun Context.createPictureFile() = try {
        File.createTempFile(imageLoader.bitmapLoader.urlLoader.md5(UUID.randomUUID().toString()), ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES))
    } catch (e: IOException) {
        null
    }

    fun takeVideo(onTaken: (Uri) -> Unit, onFailed: (() -> Unit) = {}) {
        launch {
            if (!requestPermissionsCoroutine(listOf(Manifest.permission.CAMERA))) {
                onFailed()
                return@launch
            }
            val (resultCode, data) = startActivityForResultCoroutine(
                    Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { intent ->
                        if (intent.resolveActivity(packageManager) == null) {
                            onFailed()
                            return@launch
                        }
                    })
            val uri = data?.data
            if (resultCode == Activity.RESULT_OK && uri != null) {
                onTaken(uri)
            } else {
                onFailed()
            }
        }
    }

    suspend fun takeVideoCoroutine(): Uri? =
            suspendCoroutine { continuation -> takeVideo(onTaken = { continuation.resume(it) }, onFailed = { continuation.resume(null) }) }

    fun cropPicture(picture: Uri?, aspectX: Float, aspectY: Float, onCropped: (Uri, File) -> Unit,
                    maxWidth: Int = Int.MAX_VALUE, maxHeight: Int = Int.MAX_VALUE, onFailed: (() -> Unit) = {}) {
        launch {
            picture ?: run { onFailed();return@launch }
            val file = createPictureFile() ?: run { onFailed(); return@launch }
            val uri = Uri.fromFile(file)
            val intent = UCrop.of(picture, uri).withAspectRatio(aspectX, aspectY)
                    .withMaxResultSize(maxWidth, maxHeight).getIntent(this@MultimediaActivity)
            if (intent.resolveActivity(packageManager) == null) {
                onFailed()
                return@launch
            }
            val (resultCode, _) = startActivityForResultCoroutine(intent)
            if (resultCode == Activity.RESULT_OK) {
                onCropped(uri, file)
            } else {
                onFailed()
            }
        }
    }

    suspend fun cropPictureCoroutine(picture: Uri?, aspectX: Float, aspectY: Float, maxWidth: Int = Int.MAX_VALUE, maxHeight: Int = Int.MAX_VALUE): MediaSource? =
            suspendCoroutine { continuation -> cropPicture(picture, aspectX, aspectY, maxWidth = maxWidth, maxHeight = maxHeight, onCropped = { uri, file -> continuation.resume(uri to file) }, onFailed = { continuation.resume(null) }) }
}
