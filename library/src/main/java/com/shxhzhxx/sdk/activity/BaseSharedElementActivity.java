package com.shxhzhxx.sdk.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.transition.Transition;

/**
 * 使用这个基础类需要api版本大于等于21。
 * 使用共享元素启动Activity时，必须先调用{@link #supportPostponeEnterTransition()}来修改{@link #mTransitionEnd}的状态。
 * 否则调用{@link #runAfterTransition(Runnable)}会直接执行
 *
 * 使用方式：
 * {@link #supportPostponeEnterTransition()}
 * 加载共享图片
 * {@link #supportStartPostponedEnterTransition()}
 *
 * {@link #runAfterTransition(Runnable)}调用渲染UI
 *
 * 由于java不支持多重继承，这个类通常是没法直接用的，因为每个项目基本都会有一个定制的BaseActivity需要继承。
 * 接下来需要研究一下如何用注解来自动生成代码。
 *
 */
public class BaseSharedElementActivity extends BaseActivity {
    private boolean mTransitionEnd = true, mNullTransitionException = false;
    private Runnable mRunAfterTran = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition transition = getWindow().getSharedElementEnterTransition();
            if (transition != null) {
                transition.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {

                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {
                        mTransitionEnd = true;
                        if (mRunAfterTran != null)
                            mRunAfterTran.run();
                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {

                    }

                    @Override
                    public void onTransitionPause(Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(Transition transition) {

                    }
                });
                mNullTransitionException = false;
            } else {
                // 魅族M571C( android版本5.1 )会返回null，work around.
                mNullTransitionException = true;
            }
        }
    }

    /**
     * 特殊情况无法监听到transition事件，只能根据duration来估算，如果有修改style的android:duration属性，记得同时修改{@link #transitionDuration()}的返回值。
     * */
    protected void runAfterTransition(Runnable runnable) {
        if (mNullTransitionException) {
            new Handler(Looper.getMainLooper()).postDelayed(runnable, transitionDuration());
            return;
        }
        if (mTransitionEnd)
            runnable.run();
        else
            mRunAfterTran = runnable;
    }

    protected long transitionDuration(){
        return 300;   //300 milliseconds is the default.
    }

    @Override
    public void supportPostponeEnterTransition() {
        super.supportPostponeEnterTransition();
        mTransitionEnd = false;
    }
}
