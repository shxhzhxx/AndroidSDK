package com.shxhzhxx.sdk.activity;

import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.SparseArray;
import android.widget.Toast;

import com.shxhzhxx.sdk.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class PermissionRequestActivity extends AppCompatActivity {
//    public abstract class PermissionsResultListener {
//        public void onPermissionGranted() {
//        }
//
//        public void onPermissionDenied(String[] deniedPermissions) {
//            Toast.makeText(PermissionRequestActivity.this, String.format(Locale.CHINA, getString(R.string.permission_denied),
//                    TextUtils.join("\n", deniedPermissions)), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private SparseArray<PermissionsResultListener> mPermissionCallback = new SparseArray<>();
//
//    /**
//     * 其他 Activity 继承 BaseActivity 调用 performRequestPermissions 方法
//     *
//     * @param permissions 要申请的权限数组
//     * @param listener    实现的接口
//     */
//    public void performRequestPermissions(String[] permissions, PermissionsResultListener listener) {
//        if (permissions == null || permissions.length == 0) {
//            if (listener != null) {
//                listener.onPermissionGranted();
//            }
//            return;
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkEachSelfPermission(permissions)) {
//                // 已经申请权限
//                if (listener != null) {
//                    listener.onPermissionGranted();
//                }
//            } else {
//                int requestCode = getRequestCode(mPermissionCallback);
//                mPermissionCallback.put(requestCode, listener);
//                ActivityCompat.requestPermissions(this, permissions, requestCode);
//            }
//        } else {
//            if (listener != null) {
//                listener.onPermissionGranted();
//            }
//        }
//    }
//
//
//    /**
//     * 获取一个没有被占用的requestCode
//     */
//    public <T> int getRequestCode(SparseArray<T> list) {
//        for (int code = 0; ; ++code) {
//            if (list.indexOfKey(code) < 0) {
//                return code;
//            }
//        }
//    }
//
//    /**
//     * 检察每个权限是否申请
//     *
//     * @return true 已申请权限,false 未申请权限
//     */
//    private boolean checkEachSelfPermission(String[] permissions) {
//        for (String permission : permissions) {
//            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * 申请权限结果的回调
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        PermissionsResultListener listener = mPermissionCallback.get(requestCode);
//        if (listener != null) {
//            if (grantResults.length == 0) {// If request is cancelled, the result arrays are empty.
//                listener.onPermissionDenied(permissions);
//            } else {
//                List<String> deniedPermissions = new ArrayList<>();
//                for (int i = 0; i < permissions.length; ++i) {
//                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                        deniedPermissions.add(permissions[i]);
//                    }
//                }
//                if (deniedPermissions.isEmpty()) {
//                    listener.onPermissionGranted();
//                } else {
//                    listener.onPermissionDenied(deniedPermissions.toArray(new String[]{}));
//                }
//            }
//        }
//        mPermissionCallback.remove(requestCode);
//    }
}
