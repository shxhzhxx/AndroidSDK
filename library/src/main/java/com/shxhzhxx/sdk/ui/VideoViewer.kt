package com.shxhzhxx.sdk.ui

import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.net.Uri
import android.os.Build
import android.text.format.DateUtils
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.shxhzhxx.sdk.R
import com.shxhzhxx.sdk.utils.ConditionalAction
import kotlinx.android.synthetic.main.video_viewer.view.*
import kotlinx.coroutines.*
import java.io.IOException
import kotlin.coroutines.CoroutineContext

private const val TAG = "VideoViewer"
private val STATE_SET_PLAY = intArrayOf(R.attr.state_play, -R.attr.state_pause)
private val STATE_SET_PAUSE = intArrayOf(-R.attr.state_play, R.attr.state_pause)

enum class PlayState {
    PREPARE, PLAY, STOP, RELEASE
}

class VideoViewer @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        InterceptFrameLayout(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener, CoroutineScope, SeekBar.OnSeekBarChangeListener {

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val controlDismissInterval: Long = context.theme.obtainStyledAttributes(attrs, R.styleable.VideoViewer, 0, 0)
            .let {
                val interval = try {
                    it.getInteger(R.styleable.VideoViewer_controlDismissInterval, 3000)
                } catch (e: UnsupportedOperationException) {
                    3000
                }
                it.recycle()
                return@let interval
            }.toLong()

    private val player: MediaPlayer = MediaPlayer()

    private var dismissJob: Job? = null

    private var isBuffering: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (field) {
                    loading.animate().apply { cancel() }.scaleX(1f).scaleY(1f).alpha(1f)
                    btn.animate().apply { cancel() }.scaleX(0f).scaleY(0f).alpha(0f)
                } else {
                    btn.animate().apply { cancel() }.scaleY(1f).scaleX(1f).alpha(1f)
                    loading.animate().apply { cancel() }.scaleX(0f).scaleY(0f).alpha(0f)
                }
            }
            showControlConditional["stateChange"] = true
        }

    private val playConditional: ConditionalAction = ConditionalAction(listOf("surfaceAvailable", "prepared", "start")) {
        this["start"] = false
        preview.visibility = View.INVISIBLE
        player.start()
        updateBtn()
        isBuffering = false
        showControlConditional["stateChange"] = true
        stateListener?.invoke(PlayState.PLAY)
    }
    private val showControlConditional = ConditionalAction(listOf("dataSource", "stateChange")) {
        controlLayout.visibility = View.VISIBLE
        dismissJob?.cancel()
        if (player.isPlaying && !isBuffering)
            dismissJob = launch { delay(controlDismissInterval);controlLayout.visibility = View.INVISIBLE }
    }

    var stateListener: ((state: PlayState) -> Unit)? = null

    private var seekBarDragging = false

    private var isPrepared = false

    fun hideControlPanel() {
        controlLayout.visibility = View.INVISIBLE
    }

    init {
        if (context is FragmentActivity) {
            context.lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    release()
                }
            })
        }
        interceptor = { job.isCancelled }
        player.setOnPreparedListener {
            adjustAspectRatio(it.videoWidth, it.videoHeight)
            val duration = player.duration
            seekBar.max = duration
            endText.text = DateUtils.formatElapsedTime((duration / 1000).toLong())
            player.start()
            player.pause()

            playConditional["prepared"] = true
            isPrepared = true
            stateListener?.invoke(PlayState.PREPARE)
        }
        player.setOnCompletionListener {
            updateBtn()
            showControlConditional["stateChange"] = true
            stateListener?.invoke(PlayState.STOP)
        }
        player.setOnBufferingUpdateListener { _, percent ->
            seekBar.secondaryProgress = percent * seekBar.max / 100
        }
        player.setOnSeekCompleteListener {
            isBuffering = false
        }
        player.setOnInfoListener { _, what, _ ->
            when (what) {
                MEDIA_INFO_BUFFERING_START -> isBuffering = true
                MEDIA_INFO_BUFFERING_END -> isBuffering = false
            }
            return@setOnInfoListener false
        }
        player.setOnErrorListener { _, what, _ ->
            if (what == MEDIA_ERROR_SERVER_DIED) {
                playConditional.reset()
                showControlConditional.reset()
                hideControlPanel()
                release()
            }
            return@setOnErrorListener true
        }

        LayoutInflater.from(context).inflate(R.layout.video_viewer, this, true)

        launch {
            while (isActive) {
                delay(1000)
                if (!seekBarDragging && isPrepared)
                    seekBar.progress = player.currentPosition
            }
        }
        controlLayout.visibility = View.INVISIBLE
        textureView.surfaceTextureListener = this
        textureView.setOnClickListener {
            if (controlLayout.visibility == View.VISIBLE) {
                hideControlPanel()
            } else {
                showControlConditional["stateChange"] = true
            }
        }

        btn.setOnClickListener {
            if (player.isPlaying) {
                pause()
            } else {
                start()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btn.setImageResource(R.drawable.asl_play_pause)
        }
        loading.scaleX = 0f
        loading.scaleY = 0f
        loading.alpha = 0f
        updateBtn()

        seekBar.setOnSeekBarChangeListener(this)
    }

    @Throws(IOException::class)
    fun setDataSource(path: String) {
        if (job.isCancelled)
            return
        reset()

        player.setDataSource(path)
        player.prepareAsync()
        updateBtn()
        showControlConditional["dataSource"] = true
        showControlConditional["stateChange"] = true
    }

    fun setDataSource(uri: Uri) {
        if (job.isCancelled)
            return
        reset()

        player.setDataSource(context, uri)
        player.prepareAsync()
        updateBtn()
        showControlConditional["dataSource"] = true
        showControlConditional["stateChange"] = true
    }

    fun start() {
        if (job.isCancelled)
            return
        playConditional["start"] = true
    }

    fun reset() {
        if (job.isCancelled)
            return
        playConditional["prepared"] = false
        showControlConditional["dataSource"] = false
        isPrepared = false

        controlLayout.visibility = View.INVISIBLE
        if (player.isPlaying) {
            player.stop()
            stateListener?.invoke(PlayState.STOP)
        }
        player.reset()
        seekBar.progress = 0
        updateBtn()
    }

    fun pause() {
        playConditional["start"] = false
        showControlConditional["stateChange"] = true
        player.pause()
        updateBtn()
        stateListener?.invoke(PlayState.STOP)
    }

    fun release() {
        if (job.isCancelled)
            return
        player.release()
        job.cancel()
        stateListener?.invoke(PlayState.RELEASE)
    }

    val screenshot get() = textureView.bitmap

    fun getPreview(): ImageView = preview

    private fun updateBtn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btn.setImageState(if (player.isPlaying) STATE_SET_PAUSE else STATE_SET_PLAY, true)
        } else {
            btn.setImageResource(if (player.isPlaying) R.drawable.ic_pause_white_24dp else R.drawable.ic_play_arrow_white_24dp)
        }
    }

    private fun adjustAspectRatio(videoWidth: Int, videoHeight: Int) {
        val viewWidth = textureView.width
        val viewHeight = textureView.height
        val aspectRatio = videoHeight.toDouble() / videoWidth

        val newWidth: Int
        val newHeight: Int
        if (viewHeight > (viewWidth * aspectRatio).toInt()) {
            // limited by narrow width; restrict height
            newWidth = viewWidth
            newHeight = (viewWidth * aspectRatio).toInt()
        } else {
            // limited by short height; restrict width
            newWidth = (viewHeight / aspectRatio).toInt()
            newHeight = viewHeight
        }
        val xoff = (viewWidth - newWidth) / 2
        val yoff = (viewHeight - newHeight) / 2

        val txform = Matrix()
        textureView.getTransform(txform)
        txform.setScale(newWidth.toFloat() / viewWidth, newHeight.toFloat() / viewHeight)
        //txform.postRotate(10);          // just for fun
        txform.postTranslate(xoff.toFloat(), yoff.toFloat())
        textureView.setTransform(txform)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        startText.text = DateUtils.formatElapsedTime((progress / 1000).toLong())
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        seekBarDragging = true
        dismissJob?.cancel()
    }

    override fun onStopTrackingTouch(_seekBar: SeekBar?) {
        seekBarDragging = false
        isBuffering = true
        player.seekTo(seekBar.progress)
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceTextureAvailable")
        if (!job.isCancelled) {
            player.setSurface(Surface(surface))
            playConditional["surfaceAvailable"] = true
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceTextureSizeChanged")
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        Log.d(TAG, "onSurfaceTextureDestroyed")
        if (!job.isCancelled) {
            playConditional["surfaceAvailable"] = false
            player.pause()
            player.setSurface(null)
            updateBtn()
            stateListener?.invoke(PlayState.STOP)
        }
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
}
