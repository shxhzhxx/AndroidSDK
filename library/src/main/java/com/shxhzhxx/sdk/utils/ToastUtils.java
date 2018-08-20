package com.shxhzhxx.sdk.utils;

import android.content.Context;
import android.widget.Toast;

public abstract class ToastUtils {
    private static Toast mToast;
    public static void init(Context context){
        mToast=Toast.makeText(context,"",Toast.LENGTH_SHORT);
    }
    public static void show(String msg){
        mToast.setText(msg);
        mToast.cancel();
        mToast.show();
    }
}
