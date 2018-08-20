package com.shxhzhxx.sdk.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.DisplayMetrics;

public abstract class Res {
    private static Resources res;
    private static DisplayMetrics displayMetrics;

    public static void init(Context context) {
        res = context.getResources();
        displayMetrics = context.getResources().getDisplayMetrics();
    }

    public static String getString(int id) {
        return res.getString(id);
    }

    public static int getColor(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return res.getColor(id, null);
        } else {
            return res.getColor(id);
        }
    }

    public static Bitmap getBitmap(int id) {
        return BitmapFactory.decodeResource(res, id);
    }

    public static float getDimension(int id){
        return res.getDimension(id);
    }

    public static int calPx(int dp) {
        final float scale = displayMetrics.density;
        return (int) (dp * scale + 0.5f);
    }

    public static int getStatusBarHeight() {
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId != 0) {
            return res.getDimensionPixelSize(resourceId);
        }
        return calPx(24);
    }
}
