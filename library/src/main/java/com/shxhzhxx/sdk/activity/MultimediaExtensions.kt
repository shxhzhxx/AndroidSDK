package com.shxhzhxx.sdk.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.shxhzhxx.sdk.imageLoader
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

enum class DocumentType(val value: String) {
    VIDEO("video/*"),
    IMAGE("image/*"),
    ALL("*/*")
}

data class MediaSource(val uri: Uri, val file: File)

infix fun Uri.to(that: File): MediaSource = MediaSource(this, that)

suspend fun ForResultActivity.openDocumentCoroutine(type: DocumentType = DocumentType.IMAGE, onFailure: (() -> Unit)? = null): Uri =
        try {
            suspendCancellableCoroutine { continuation ->
                openDocument(type, onOpen = { continuation.resume(it) },
                        onFailure = { continuation.resumeWithException(CancellationException()) })
            }
        } catch (e: CancellationException) {
            onFailure?.invoke()
            throw e
        }

fun ForResultActivity.openDocument(type: DocumentType = DocumentType.IMAGE, onOpen: (Uri) -> Unit, onFailure: (() -> Unit)? = null) {
    launch {
        requestPermissionsCoroutine(listOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Manifest.permission.READ_EXTERNAL_STORAGE
        } else {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        }), onFailure)

        val intent = Intent(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    Intent.ACTION_OPEN_DOCUMENT
                else
                    Intent.ACTION_GET_CONTENT
        )
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = type.value
        intent.resolveActivity(packageManager) ?: run { onFailure?.invoke(); return@launch }

        val uri = startActivityForResultCoroutine(intent, onFailure)?.data
                ?: run { onFailure?.invoke();return@launch }
        onOpen(uri)
    }
}

fun ForResultActivity.takePicture(onTake: (Uri, File) -> Unit, onFailure: (() -> Unit)? = null) {
    launch {
        requestPermissionsCoroutine(listOf(Manifest.permission.CAMERA), onFailure)
        val file = createPictureFile() ?: run { onFailure?.invoke(); return@launch }
        val uri = getUriForFile(file)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        intent.resolveActivity(packageManager) ?: run { onFailure?.invoke(); return@launch }
        startActivityForResultCoroutine(intent, onFailure)
        onTake(uri, file)
    }
}

suspend fun ForResultActivity.takePictureCoroutine(onFailure: (() -> Unit)? = null): MediaSource =
        try {
            suspendCancellableCoroutine { cancellableContinuation ->
                takePicture(onTake = { uri, file -> cancellableContinuation.resume(uri to file) },
                        onFailure = { cancellableContinuation.resumeWithException(CancellationException()) })
            }
        } catch (e: CancellationException) {
            onFailure?.invoke()
            throw e
        }

fun Context.getUriForFile(file: File): Uri = FileProvider.getUriForFile(this, "$packageName.FileProvider", file)

fun Context.createPictureFile() = try {
    File.createTempFile(imageLoader.bitmapLoader.urlLoader.md5(UUID.randomUUID().toString()), ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES))
} catch (e: IOException) {
    null
}

fun ForResultActivity.takeVideo(onTake: (Uri) -> Unit, onFailure: (() -> Unit)? = null) {
    launch {
        requestPermissionsCoroutine(listOf(Manifest.permission.CAMERA), onFailure)
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        intent.resolveActivity(packageManager) ?: run { onFailure?.invoke(); return@launch }
        val uri = startActivityForResultCoroutine(intent, onFailure)?.data
                ?: run { onFailure?.invoke();return@launch }
        onTake(uri)
    }
}

suspend fun ForResultActivity.takeVideoCoroutine(onFailure: (() -> Unit)? = null): Uri =
        try {
            suspendCancellableCoroutine { continuation ->
                takeVideo(onTake = { continuation.resume(it) },
                        onFailure = { continuation.resumeWithException(CancellationException()) })
            }
        } catch (e: CancellationException) {
            onFailure?.invoke()
            throw e
        }

fun ForResultActivity.cropPicture(picture: Uri, aspectX: Float, aspectY: Float, onCrop: (Uri, File) -> Unit,
                                  maxWidth: Int = Int.MAX_VALUE, maxHeight: Int = Int.MAX_VALUE, onFailure: (() -> Unit)? = null) {
    launch {
        val file = createPictureFile() ?: run { onFailure?.invoke(); return@launch }
        val uri = Uri.fromFile(file)
        val intent = UCrop.of(picture, uri).withAspectRatio(aspectX, aspectY)
                .withMaxResultSize(maxWidth, maxHeight).getIntent(this@cropPicture)
        intent.resolveActivity(packageManager) ?: run { onFailure?.invoke(); return@launch }
        startActivityForResultCoroutine(intent, onFailure)
        onCrop(uri, file)
    }
}

suspend fun ForResultActivity.cropPictureCoroutine(picture: Uri, aspectX: Float, aspectY: Float,
                                                   maxWidth: Int = Int.MAX_VALUE, maxHeight: Int = Int.MAX_VALUE, onFailure: (() -> Unit)? = null): MediaSource =
        try {
            suspendCancellableCoroutine { continuation ->
                cropPicture(picture, aspectX, aspectY, maxWidth = maxWidth, maxHeight = maxHeight,
                        onCrop = { uri, file -> continuation.resume(uri to file) }, onFailure = { continuation.resumeWithException(CancellationException()) })
            }
        } catch (e: CancellationException) {
            onFailure?.invoke()
            throw e
        }
