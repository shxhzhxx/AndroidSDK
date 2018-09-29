package com.shxhzhxx.sdk.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import android.util.SparseArray;

public abstract class ForResultActivity extends PermissionRequestActivity {
    public abstract static class ResultListener {
        public void onResult(int resultCode, Intent data) {
        }
    }

    public SparseArray<ResultListener> mActivityResultCallback = new SparseArray<>();

    public void startActivityForResult(Intent intent, ResultListener listener) {
        startActivityForResult(intent, listener, null);
    }

    public void startActivityForResult(Intent intent, ResultListener listener, Bundle options) {
        if (listener == null)
            return;
        int requestCode = getRequestCode(mActivityResultCallback);
        mActivityResultCallback.put(requestCode, listener);
        ActivityCompat.startActivityForResult(this, intent, requestCode, options);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mActivityResultCallback.indexOfKey(requestCode) >= 0) {
            ResultListener listener = mActivityResultCallback.get(requestCode);
            mActivityResultCallback.delete(requestCode);
            listener.onResult(resultCode, data);
        }
    }
}
