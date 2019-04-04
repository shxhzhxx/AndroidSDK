package com.shxhzhxx.sdk.ui

import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Build
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.SeekBar
import com.shxhzhxx.sdk.R
import com.shxhzhxx.sdk.utils.ConditionalAction
import kotlinx.android.synthetic.main.video_viewer.view.*
import kotlinx.coroutines.*
import java.io.IOException
import kotlin.coroutines.CoroutineContext

private const val TAG = "VideoViewer"
private val STATE_SET_PLAY = intArrayOf(R.attr.state_play, -R.attr.state_pause)
private val STATE_SET_PAUSE = intArrayOf(-R.attr.state_play, R.attr.state_pause)

class VideoViewerEx @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        FrameLayout(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener, CoroutineScope {
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

    private val playConditional: ConditionalAction = ConditionalAction(3) {
        this[2] = false
        preview.visibility = View.INVISIBLE
        player.start()
        updateBtn()
        controlConditional[1] = true
    }
    private val controlConditional = ConditionalAction(2) {
        controlLayout.visibility = View.VISIBLE
        dismissJob?.cancel()
        if (player.isPlaying)
            dismissJob = launch { delay(controlDismissInterval);controlLayout.visibility = View.INVISIBLE }
    }

    init {
        player.setOnPreparedListener {
            adjustAspectRatio(it.videoWidth, it.videoHeight)
            val duration = player.duration
            seekBar.max = duration
            endText.text = DateUtils.formatElapsedTime((duration / 1000).toLong())
            player.start()
            player.pause()

            controlConditional[1] = true
            controlConditional[0] = true
            playConditional[1] = true
        }
        player.setOnCompletionListener {
            updateBtn()
            controlConditional[1] = true
        }
        player.setOnBufferingUpdateListener { _, percent ->
            seekBar.secondaryProgress = percent * seekBar.max / 100
        }
        player.setOnErrorListener { _, _, _ ->
            playConditional.reset()
            controlConditional.reset()
            controlLayout.visibility = View.INVISIBLE
            return@setOnErrorListener true
        }

        LayoutInflater.from(context).inflate(R.layout.video_viewer, this, true)

        launch {
            while (isActive) {
                delay(1000)
                seekBar.progress = player.currentPosition
            }
        }
        controlLayout.visibility = View.INVISIBLE
        textureView.surfaceTextureListener = this
        textureView.setOnClickListener {
            if (controlLayout.visibility == View.VISIBLE) {
                dismissJob?.cancel()
                controlLayout.visibility = View.INVISIBLE
            } else {
                controlConditional[1] = true
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
        updateBtn()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(_seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                startText.text = DateUtils.formatElapsedTime((progress / 1000).toLong())
                if (fromUser) {
                    if (seekBar.secondaryProgress > progress) {
                        player.seekTo(progress)
                    } else {
                        seekBar.progress = player.currentPosition
                    }
                    controlConditional[1] = true
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                dismissJob?.cancel()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                controlConditional[1] = true
            }
        })
    }

    @Throws(IOException::class)
    fun setDataSource(path: String) {
        if (job.isCancelled)
            return
        reset()

        player.setDataSource(path)
        player.prepareAsync()
        updateBtn()
    }

    fun start() {
        if (job.isCancelled)
            return
        playConditional[2] = true
    }

    fun reset() {
        if (job.isCancelled)
            return
        playConditional[1] = false
        controlConditional[0] = false

        controlLayout.visibility = View.INVISIBLE
        if (player.isPlaying)
            player.stop()
        player.reset()
        seekBar.progress = 0
        updateBtn()
    }

    fun pause() {
        playConditional[2] = false
        player.pause()
        updateBtn()
        controlConditional[1] = true
    }

    fun release() {
        if (job.isCancelled)
            return
        player.release()
        job.cancel()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return job.isCancelled || super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return job.isCancelled || super.onInterceptTouchEvent(ev)
    }

    val screenshot get() = textureView.bitmap

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

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (!job.isCancelled) {
            player.setSurface(Surface(surface))
            playConditional[0] = true
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        if (!job.isCancelled) {
            playConditional[0] = false
            player.pause()
            player.setSurface(null)
            updateBtn()
        }
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
}
