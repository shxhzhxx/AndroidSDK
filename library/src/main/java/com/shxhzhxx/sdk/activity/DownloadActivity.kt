package com.shxhzhxx.sdk.activity

//import android.annotation.SuppressLint;
//import android.app.DownloadManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.Build;
//import android.provider.Settings;
//
//import com.shxhzhxx.sdk.utils.FileUtils;
//
//import java.util.HashMap;

//public abstract class DownloadActivity extends MultimediaActivity {
//    public abstract class DownloadListener {
//        public void onResult(Uri uri) {
//        }
//
//        public void onStart(long id) {
//        }
//
//        public void onClick() {
//        }
//    }
//
//    @SuppressLint("UseSparseArrays")
//    private HashMap<Long, DownloadListener> mDownloadCallback = new HashMap<>();
//    private BroadcastReceiver receiver = null;
//    private BroadcastReceiver clickReceiver = null;
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (receiver != null)
//            unregisterReceiver(receiver);
//        if (clickReceiver != null)
//            unregisterReceiver(clickReceiver);
//    }
//
//    private void ensureReceiver() {
//        if (receiver == null) {
//            receiver = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
//                    DownloadListener listener = mDownloadCallback.get(completeDownloadId);
//                    if (listener != null) {
//                        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//                        Uri uri = null;
//                        if (dm != null) {
//                            uri = dm.getUriForDownloadedFile(completeDownloadId);
//                        }
//                        listener.onResult(uri);
//                    }
//                    mDownloadCallback.remove(completeDownloadId);
//                }
//            };
//            registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
//        }
//        if (clickReceiver == null) {
//            clickReceiver = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    Intent dm = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
//                    dm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    if (dm.resolveActivity(getPackageManager()) != null) {
//                        context.startActivity(dm);
//                    } else {
//                        long[] ids = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
//                        for (long id : ids) {
//                            DownloadListener listener = mDownloadCallback.get(id);
//                            if (listener != null) {
//                                listener.onClick();
//                            }
//                        }
//                    }
//                }
//            };
//            registerReceiver(clickReceiver, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
//        }
//    }
//
//    protected void download(String url, DownloadListener listener) {
//        ensureReceiver();
//        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
//
////         通过setAllowedNetworkTypes方法可以设置允许在何种网络下下载，
////         也可以使用setAllowedOverRoaming方法，它更加灵活
////        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
//        req.setAllowedOverRoaming(false);
//
//        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//
//        // 设置下载文件存放的路径，同样你可以选择以下方法存放在你想要的位置。
//        // setDestinationUri
//        // setDestinationInExternalPublicDir
////        req.setDestinationInExternalFilesDir(mContext, Environment.DIRECTORY_DOWNLOADS, fileName);
//
//
//        // 设置一些基本显示信息
////        req.setTitle("name.apk");
////        req.setDescription("下载完后请点击打开");
////        req.setMimeType("application/vnd.android.package-archive");
//
//        // Ok go!
//        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//        long downloadId = dm.enqueue(req);
//        if (listener != null) {
//            listener.onStart(downloadId);
//        }
//        mDownloadCallback.put(downloadId, listener);
//    }
//
//    protected void cancelDownload(long id) {
//        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//        dm.remove(id);
//    }
//
//    protected void promptInstall(Uri uri) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !getPackageManager().canRequestPackageInstalls()) {
//            final Uri finalUri = uri;
//            startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName())), new ResultListener() {
//                @Override
//                public void onResult(int resultCode, Intent data) {
//                    if (resultCode == RESULT_OK) {
//                        promptInstall(finalUri);
//                    }
//                }
//            });
//            return;
//        }
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        // FLAG_ACTIVITY_NEW_TASK 可以保证安装成功时可以正常打开 app
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        } else {
//            if ("content".equals(uri.getScheme())) {//api24以下版本不支持scheme为content的uri，转换为path
//                uri = FileUtils.getUriForFile(this, FileUtils.getFileByUri(this, uri));
//            }
//        }
//        intent.setDataAndType(uri, "application/vnd.android.package-archive");
//        startActivity(intent);
//    }
//
//    public int[] getBytesAndStatus(long downloadId) {
//        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//        int[] bytesAndStatus = new int[]{-1, -1, 0};
//        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
//        Cursor c = null;
//        try {
//            c = dm.query(query);
//            if (c != null && c.moveToFirst()) {
//                bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
//                bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
//                bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
//            }
//        } finally {
//            if (c != null) {
//                c.close();
//            }
//        }
//        return bytesAndStatus;
//    }
//}
