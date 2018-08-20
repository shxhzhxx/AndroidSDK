package com.shxhzhxx.sdk.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.UUID;

public abstract class Settings {
    private static final String TAG = Settings.class.getSimpleName();
    private static final String KEY_INSTANCE_ID = "com.shxhzhxx.sdk.utils.instanceId";
    private static SharedPreferences sharedPreferences;
    private static WeakReference<Context> mContext;

    public static void init(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = new WeakReference<>(context);
    }

    public static String getString(String key) {
        return getString(key, "");
    }

    public static String getString(String key, String defValue) {
        try {
            return sharedPreferences.getString(key, defValue);
        } catch (ClassCastException e) {
            Log.e(TAG, "Get ClassCastException when get " + key + " value", e);
            return defValue;
        }
    }

    public static void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public static boolean getBoolean(String key, boolean defValue) {
        try {
            return sharedPreferences.getBoolean(key, defValue);
        } catch (ClassCastException e) {
            Log.e(TAG, "Get ClassCastException when get " + key + " value", e);
            return defValue;
        }
    }

    public static void putBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public static int getInt(String key, int defValue) {
        try {
            return sharedPreferences.getInt(key, defValue);
        } catch (ClassCastException e) {
            Log.e(TAG, "Get ClassCastException when get " + key + " value", e);
            return defValue;
        }
    }

    public static void putInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public static long getLong(String key, long defValue) {
        try {
            return sharedPreferences.getLong(key, defValue);
        } catch (ClassCastException e) {
            Log.e(TAG, "Get ClassCastException when get " + key + " value", e);
            return defValue;
        }
    }

    public static void putLong(String key, long value) {
        sharedPreferences.edit().putLong(key, value).apply();
    }

    public static String getInstanceId() {
        String instanceId = getString(KEY_INSTANCE_ID, null);
        if (instanceId == null) {
            instanceId = UUID.randomUUID().toString();
            putString(KEY_INSTANCE_ID, instanceId);
        }
        return instanceId;
    }

    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
    @Nullable
    public static String getDeviceId() {
        Context context = mContext.get();
        if (context == null)
            return null;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null)
            return null;
        String imei;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            imei = telephonyManager.getImei();
        } else {
            imei = telephonyManager.getDeviceId();
        }

        if (imei != null)
            return imei;
        return telephonyManager.getSubscriberId();
    }
}
