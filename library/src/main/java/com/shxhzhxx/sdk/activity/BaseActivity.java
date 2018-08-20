package com.shxhzhxx.sdk.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.shxhzhxx.imageloader.ImageLoader;
import com.shxhzhxx.sdk.Net;

public class BaseActivity extends DownloadActivity implements View.OnClickListener {
    protected String TAG = this.getClass().getSimpleName();
    protected String IDENTIFY = String.valueOf(hashCode());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.add(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.remove(this);
        Net.getInstance().cancelByTag(IDENTIFY);
        ImageLoader.getInstance().cancelByTag(IDENTIFY);
    }

    protected void setOnClickListener(View[] views) {
        for (View v : views) {
            v.setOnClickListener(this);
        }
    }

    protected void setOnClickListener(int[] ids) {
        for (int id : ids) {
            findViewById(id).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {

    }

    protected void setStatusBarColor(int color, boolean lightStatusBar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            int visibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            if (lightStatusBar) {
                visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            window.getDecorView().setSystemUiVisibility(visibility);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }
}
