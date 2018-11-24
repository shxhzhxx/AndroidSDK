package com.shxhzhxx.sdk.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.shxhzhxx.sdk.R;
import com.shxhzhxx.sdk.utils.ConditionRunnable;

import java.io.IOException;


/**
 * surface------------------------0----|
 * * * * * * * * * * * * * * * * * * * |
 * setData --- prepared-----------1----|---------play
 * * * * * * * * * * * * * * * * * * * |
 * start--------------------------2----|
 */

public class VideoViewer extends FrameLayout implements TextureView.SurfaceTextureListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnErrorListener {
    private static final String TAG = "VideoViewer";
    private static final int[] STATE_SET_PLAY =
            {R.attr.state_play, -R.attr.state_pause};
    private static final int[] STATE_SET_PAUSE =
            {-R.attr.state_play, R.attr.state_pause};
    private int CONTROL_DISMISS_INTERVAL;

    private TextureView mTextureView;
    private MediaPlayer mPlayer;
    private SeekBar mSeekBar;
    private ImageView mBtn, mPreview;
    private TextView mStartText, mEndText;
    private ViewGroup mControlLayout;
    private boolean mFileSource = false;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mControlDismissRun = new Runnable() {
        @Override
        public void run() {
            mControlLayout.setVisibility(INVISIBLE);
        }
    };
    private Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            mSeekBar.setProgress(mPlayer.getCurrentPosition());
            if (mPlayer.isPlaying())
                mHandler.postDelayed(this, 500);
        }
    };
    private ConditionRunnable mPlayCondRun = new ConditionRunnable(3) {
        @Override
        public void run() {
            mPlayCondRun.setCond(2,false);
            mPreview.setVisibility(INVISIBLE);
            mPlayer.start();
            updateBtn();
            mUpdateProgressTask.run();
            mShowCtrlCondRun.setCond(1,true);
        }
    };
    private ConditionRunnable mShowCtrlCondRun = new ConditionRunnable(2) {
        @Override
        public void run() {
            mControlLayout.setVisibility(VISIBLE);
            mHandler.removeCallbacks(mControlDismissRun);
            if (mPlayer.isPlaying())
                mHandler.postDelayed(mControlDismissRun, CONTROL_DISMISS_INTERVAL);
        }
    };


    public VideoViewer(@NonNull Context context) {
        this(context, null);
    }

    public VideoViewer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoViewer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnBufferingUpdateListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnCompletionListener(this);


        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.VideoViewer, 0, 0);
        try {
            CONTROL_DISMISS_INTERVAL = a.getInteger(R.styleable.VideoViewer_controlDismissInterval, 3000);
        } finally {
            a.recycle();
        }

        View view = LayoutInflater.from(context).inflate(R.layout.video_viewer, this, true);

        mPreview = view.findViewById(R.id.preview);
        mControlLayout = view.findViewById(R.id.controlLayout);
        mControlLayout.setVisibility(INVISIBLE);
        mTextureView = view.findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setOnClickListener(this);

        mBtn = view.findViewById(R.id.btn);
        mBtn.setOnClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBtn.setImageResource(R.drawable.asl_play_pause);
        }
        updateBtn();

        mSeekBar = view.findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mStartText = view.findViewById(R.id.startText);
        mEndText = view.findViewById(R.id.endText);
    }

    public void setDataSource(String path) throws IOException {
        if (mReleased)
            return;
        reset();

        String scheme = Uri.parse(path).getScheme();
        mFileSource = scheme == null || "file".equals(scheme);
        mPlayer.setDataSource(path);
        mPlayer.prepareAsync();
        updateBtn();
    }

    public void start() {
        if (mReleased)
            return;
        mPlayCondRun.setCond(2,true);
    }

    public void reset(){
        if (mReleased)
            return;
        mPlayCondRun.setCond(1,false);
        mShowCtrlCondRun.setCond(0, false);

        mControlDismissRun.run();
        if (mPlayer.isPlaying())
            mPlayer.stop();
        mPlayer.reset();
        mSeekBar.setProgress(0);
        updateBtn();
    }

    public void pause() {
        mPlayCondRun.setCond(2,false);
        mPlayer.pause();
        updateBtn();
        mShowCtrlCondRun.setCond(1,true);
    }

    /**
     * deny all access from touchEvent and func after {@link #release()}
     */
    private boolean mReleased = false;

    public void release() {
        if (mReleased)
            return;
        mPlayer.release();
        mReleased = true;
        mHandler.removeCallbacks(mControlDismissRun);
        mHandler.removeCallbacks(mUpdateProgressTask);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mReleased || super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mReleased || super.onInterceptTouchEvent(ev);
    }

    public ImageView getPreview() {
        return mPreview;
    }

    public Bitmap screenShot() {
        return mTextureView.getBitmap();
    }

    private void updateBtn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBtn.setImageState(mPlayer.isPlaying() ? STATE_SET_PAUSE : STATE_SET_PLAY, true);
        } else {
            mBtn.setImageResource(mPlayer.isPlaying() ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp);
        }
    }

    private void adjustAspectRatio(int videoWidth, int videoHeight) {
        int viewWidth = mTextureView.getWidth();
        int viewHeight = mTextureView.getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;

        Matrix txform = new Matrix();
        mTextureView.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        //txform.postRotate(10);          // just for fun
        txform.postTranslate(xoff, yoff);
        mTextureView.setTransform(txform);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (!mReleased) {
            mPlayer.setSurface(new Surface(surface));
            mPlayCondRun.setCond(0,true);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (!mReleased) {
            mPlayCondRun.setCond(0,false);
            mPlayer.pause();
            mPlayer.setSurface(null);
            updateBtn();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn) {
            if (mPlayer.isPlaying()) {
                pause();
            } else {
                start();
            }
        } else {
            if (mControlLayout.getVisibility() == VISIBLE) {
                mHandler.removeCallbacks(mControlDismissRun);
                mControlDismissRun.run();
            } else {
                mShowCtrlCondRun.setCond(1,true);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mStartText.setText(DateUtils.formatElapsedTime(progress / 1000));
        if (fromUser) {
            if (mSeekBar.getSecondaryProgress() > progress) {
                mPlayer.seekTo(progress);
            } else {
                mSeekBar.setProgress(mPlayer.getCurrentPosition());
            }
            mShowCtrlCondRun.setCond(1,true);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mControlDismissRun);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mShowCtrlCondRun.setCond(1,true);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        updateBtn();
        mShowCtrlCondRun.setCond(1,true);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        adjustAspectRatio(mp.getVideoWidth(), mp.getVideoHeight());
        int duration = mPlayer.getDuration();
        mSeekBar.setMax(duration);
        if (mFileSource) {
            onBufferingUpdate(mPlayer, 100);
        }
        mEndText.setText(DateUtils.formatElapsedTime(duration / 1000));
        mPlayer.start();
        mPlayer.pause();

        mShowCtrlCondRun.setCond(1,true);
        mShowCtrlCondRun.setCond(0,true);
        mPlayCondRun.setCond(1,true);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mSeekBar.setSecondaryProgress(percent * mSeekBar.getMax() / 100);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mPlayCondRun.reset();
        mShowCtrlCondRun.reset();
        mControlDismissRun.run();
        return true;
    }
}
