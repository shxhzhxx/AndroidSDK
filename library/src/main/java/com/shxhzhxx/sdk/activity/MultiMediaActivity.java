package com.shxhzhxx.sdk.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.shxhzhxx.imageloader.BitmapLoader;
import com.shxhzhxx.sdk.utils.FileUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

public abstract class MultiMediaActivity extends ForResultActivity {
//    public abstract static class GetMediaListener {
//        public void onSuccess(Uri uri) {
//        }
//
//        public void onFailed() {
//        }
//
//        public void onRotate() {
//        }
//    }
//
//    @Nullable
//    private static Bitmap loadRotateBitmap(File file, int width, int height, int degree) {
//        int w, h;
//        switch (degree) {
//            case 90:
//            case 270:
//                w = height;
//                h = width;
//                break;
//            case 180:
//                w = width;
//                h = height;
//                break;
//            default:
//                return BitmapLoader.loadBitmap(file, width, height);
//        }
//        Bitmap bitmap = BitmapLoader.loadBitmap(file, w, h);
//        if (bitmap == null)
//            return null;
//        Matrix matrix = new Matrix();
//        matrix.postRotate(degree);
//        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//    }
//
//    private static int readDegree(File file) {
//        if (file == null || !file.exists())
//            return -1;
//        try {
//            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
//            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//            switch (orientation) {
//                case ExifInterface.ORIENTATION_NORMAL:
//                    return 0;
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    return 90;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    return 180;
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    return 270;
//            }
//        } catch (IOException ignore) {
//        }
//        return -1;
//    }
//
//    private static void reviseRotateImageFile(File file, int width, int height) {
//        int degree = readDegree(file);
//        if (degree > 0) {
//            Bitmap bitmap = loadRotateBitmap(file, width, height, degree);
//            if (bitmap == null)
//                return;
//            try {
//                bitmap.compress(Bitmap.CompressFormat.PNG, 0, new FileOutputStream(file));
//            } catch (FileNotFoundException ignore) {
//            }
//        }
//    }
//
//    public void getPhotoByCamera(GetMediaListener listener) {
//        getPhotoByCamera(0, 0, listener);
//    }
//
//    public void getPhotoByCamera(final int width, final int height, final GetMediaListener listener) {
//        if (listener == null)
//            return;
//        performRequestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionsResultListener() {
//            @Override
//            public void onPermissionGranted() {
//                final Uri uri;
//                final File file;
//                try {
//                    file = FileUtils.createExternalCacheFile("png");
//                    uri = FileUtils.getUriForFile(MultiMediaActivity.this, file);
//                } catch (IOException e) {
//                    listener.onFailed();
//                    return;
//                }
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                }
//                if (intent.resolveActivity(getPackageManager()) != null) {
//                    startActivityForResult(Intent.createChooser(intent, null), new ResultListener() {
//                        @Override
//                        public void onResult(int resultCode, Intent data) {
//                            if (resultCode == RESULT_OK) {
//                                listener.onRotate();
//                                new Thread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        reviseRotateImageFile(file, width, height);
//                                        runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                listener.onSuccess(Uri.fromFile(file));
//                                            }
//                                        });
//                                    }
//                                }).start();
//                            } else {
//                                listener.onFailed();
//                            }
//                        }
//                    });
//                } else {
//                    listener.onFailed();
//                }
//            }
//        });
//    }
//
//    public void getPhotoByAlbum(final GetMediaListener listener) {
//        pick("image/*", listener);
//    }
//
//    public void getVideo(GetMediaListener listener) {
//        pick("video/*", listener);
//    }
//
//    public void pick(final String type, final GetMediaListener listener) {
//        if (listener == null)
//            return;
//        performRequestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionsResultListener() {
//            @Override
//            public void onPermissionGranted() {
//                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent.setType(type);
//                if (intent.resolveActivity(getPackageManager()) != null) {
//                    startActivityForResult(Intent.createChooser(intent, null), new ResultListener() {
//                        @Override
//                        public void onResult(int resultCode, Intent data) {
//                            if (resultCode == RESULT_OK) {
//                                listener.onSuccess(data.getData());
//                            } else {
//                                listener.onFailed();
//                            }
//                        }
//                    });
//                } else {
//                    listener.onFailed();
//                }
//            }
//        });
//    }
//
//    public void cropPhoto(Uri src, int aspectX, int aspectY, final GetMediaListener listener) {
//        if (src == null || aspectX <= 0 || aspectY <= 0 || listener == null) {
//            return;
//        }
//        final Uri out;
//        try {
//            File file = FileUtils.createExternalCacheFile("png");
//            out = Uri.fromFile(file);
//        } catch (IOException e) {
//            listener.onFailed();
//            return;
//        }
//
//        Intent intent = UCrop.of(src, out).withAspectRatio(aspectX, aspectY).getIntent(this);
//
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(intent, new ResultListener() {
//                @Override
//                public void onResult(int resultCode, Intent data) {
//                    if (resultCode == RESULT_OK) {
//                        listener.onSuccess(out);
//                    } else {
//                        listener.onFailed();
//                    }
//                }
//            });
//        } else {
//            listener.onFailed();
//        }
//    }
//
//    public void getFile(final GetMediaListener listener) {
//        if (listener == null)
//            return;
//        performRequestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionsResultListener() {
//            @Override
//            public void onPermissionGranted() {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("*/*");
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                if (intent.resolveActivity(getPackageManager()) != null) {
//                    startActivityForResult(Intent.createChooser(intent, null), new ResultListener() {
//                        @Override
//                        public void onResult(int resultCode, Intent data) {
//                            if (resultCode == RESULT_OK) {
//                                listener.onSuccess(data.getData());
//                            } else {
//                                listener.onFailed();
//                            }
//                        }
//                    });
//                } else {
//                    listener.onFailed();
//                }
//            }
//        });
//    }
}
