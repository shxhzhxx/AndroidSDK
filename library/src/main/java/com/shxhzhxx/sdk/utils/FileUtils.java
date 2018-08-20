package com.shxhzhxx.sdk.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Locale;
import java.util.UUID;

public abstract class FileUtils {
    private static File mExternalCacheDir;
    private static File mCacheDir;
    private static ContentResolver mContentResolver;

    public static void init(Context context) {
        mExternalCacheDir = context.getExternalCacheDir();
        mCacheDir = context.getCacheDir();
        mContentResolver = context.getContentResolver();
    }

    public static File createExternalCacheFile(String suffix) throws IOException {
        return createFile(mExternalCacheDir, suffix);
    }

    public static File createExternalCacheFile() throws IOException {
        return createExternalCacheFile(null);
    }

    public static File createCacheFile(String suffix) throws IOException {
        return createFile(mCacheDir, suffix);
    }

    public static File createCacheFile() throws IOException {
        return createCacheFile(null);
    }

    private static File createFile(File dir, String suffix) throws IOException {
        String name = String.valueOf(UUID.randomUUID());
        if (!TextUtils.isEmpty(suffix)) {
            name += "." + suffix;
        }
        File file = new File(dir, name);
        if (!file.createNewFile() && (!file.delete() || !file.createNewFile())) {
            throw new IOException("createFile failed");
        }
        return file;
    }

    public static boolean copyFile(File src, File dst) {
        if (dst.exists())
            if (!dst.delete())
                return false;
        try {
            FileChannel in = new FileInputStream(src).getChannel();
            FileChannel out = new FileOutputStream(dst).getChannel();
            out.transferFrom(in, 0, in.size());
            in.close();
            out.close();
            return true;
        } catch (IOException ignore) {
            return false;
        }
    }


    public static Uri getUriForFile(Context context, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
            uri = FileProvider.getUriForFile(context, String.format(Locale.CHINA,"%s.FileProvider",context.getPackageName()), file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    public static File getFileByUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        File file = new File(uri.getPath());
        if (file.exists()) {
            return file;
        }
        Cursor cursor = mContentResolver.query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int dataColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                if (dataColumn != -1) {
                    return new File(cursor.getString(dataColumn));
                }
            } else {
                cursor.close();
            }
        }
        return new File(getFilePathFromURI(context, uri));//终极处理手段，这个方法有待研究
    }


    public static String getFilePathFromURI(Context context, Uri uri) {
        if (null == uri) {
            return null;
        }
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = mContentResolver.query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
