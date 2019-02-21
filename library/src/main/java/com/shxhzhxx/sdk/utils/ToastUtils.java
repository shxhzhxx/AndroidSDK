package com.shxhzhxx.sdk.utils;

import android.content.Context;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public abstract class ToastUtils {
    private static Toast mToast;
    private static WeakReference<Context> mContextRef;

    public static void init(Context context) {
        mContextRef = new WeakReference<>(context);
    }

    public static void show(String msg) {
        Context context = mContextRef.get();
        if (context == null)
            return;
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
