package com.shxhzhxx.sdk;

import android.os.Build;

import com.shxhzhxx.imageloader.ImageLoader;
import com.shxhzhxx.sdk.network.Net;
import com.shxhzhxx.sdk.utils.FileUtils;
import com.shxhzhxx.sdk.utils.Res;
import com.shxhzhxx.sdk.utils.Settings;
import com.shxhzhxx.sdk.utils.ToastUtils;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoader.init(this);
        Net.init(this);
        FileUtils.init(this);
        Settings.init(this);
        Res.init(this);
        ToastUtils.init(this);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL || level == TRIM_MEMORY_RUNNING_LOW || level == TRIM_MEMORY_RUNNING_MODERATE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                ImageLoader.getInstance().getBitmapLoader().trimMemory(false);
            }
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            ImageLoader.getInstance().getBitmapLoader().trimMemory(true);
        }
    }
}
