package com.shxhzhxx.sdk.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

abstract class CoroutineActivity : AppCompatActivity(), CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addActivity(this)
    }
    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        removeActivity(this)
    }
}

//    protected void setStatusBarColor(int color, boolean lightStatusBar) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Window window = getWindow();
//            int visibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//            if (lightStatusBar) {
//                visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
//            }
//            window.getDecorView().setSystemUiVisibility(visibility);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(color);
//        }
//    }
//
//    /**
//     * 这种全屏模式是隐藏状态栏的。
//     * <p>
//     * 如果想要布局与状态栏重叠，可以使用{@link #setStatusBarColor(int, boolean)}，
//     * 第一个参数传{@link android.graphics.Color#TRANSPARENT}，第二个参数根据布局颜色确定；
//     * 布局根视图设置{@link View#setFitsSystemWindows(boolean)}为false（默认属性）。
//     */
//    protected void fullscreen() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            getWindow().addFlags(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//        }
//    }