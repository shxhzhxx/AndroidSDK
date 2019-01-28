package com.shxhzhxx.sdk.network;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class DbManager {
    private static final String TAG = "DbManager";

    public abstract static class Callback {
        public void onScanSuccess(String ip) {
        }

        public void onFinish(boolean success) {
        }
    }

    private static Handler mMainHandler = new Handler(Looper.getMainLooper());
    private static Thread mScanThread;
    private static WifiManager mWifiManager;

    public static void init(Context context) {
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public static void cancel() {
        if (mScanThread != null) {
            mScanThread.interrupt();
        }
    }

    public static void scan(int port, final Callback callback) {
        cancel();
        mScanThread = new ScanThread(port, callback);
        mScanThread.start();
    }


    private static class ScanThread extends Thread {
        transient Callback mCallback;
        final int mPort;
        private static final int TIME_OUT = 10000;
        volatile boolean mCancel = false;

        ScanThread(int port, Callback callback) {
            mPort = port;
            mCallback = callback;
        }

        @Override
        public void interrupt() {
            super.interrupt();
            mCancel = true;
        }

        private void returnFailed() {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!mCancel)
                        mCallback.onFinish(false);
                }
            });
        }

        @Override
        public void run() {
            int ip = mWifiManager.getConnectionInfo().getIpAddress();
            if (ip == 0) {
                returnFailed();
                return;
            }

            Selector selector;
            try {
                selector = Selector.open();
            } catch (IOException e) {
                Log.d(TAG, "open failed :" + e.getMessage());
                returnFailed();
                return;
            }

            //子网的第一个ip地址用于标识子网，最后一个ip地址作为广播地址。C类子网的 xxx.xxx.xxx.0 和 xxx.xxx.xxx.255 不可用于设备ip
            for (int n = 0; n < 256; ++n) {
                if (mCancel)
                    break;
                if (n == ((ip >> 24) & 0xff)) {
                    continue;
                }
                final byte[] host = new byte[4];
                host[0] = (byte) (ip & 0xff);
                host[1] = (byte) ((ip >> 8) & 0xff);
                host[2] = (byte) ((ip >> 16) & 0xff);
                host[3] = (byte) n;

                InetAddress addr;
                SocketChannel channel;
                try {
                    addr = InetAddress.getByAddress(host);
                    channel = SocketChannel.open();
                } catch (IOException e) {
                    Log.d(TAG, "create channel failed :" + e.getMessage());
                    continue;
                }
                try {
                    channel.configureBlocking(false);
                    if (channel.connect(new InetSocketAddress(addr, mPort))) {
                        final String address = addr.getHostAddress();
                        mMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!mCancel)
                                    mCallback.onScanSuccess(address);
                            }
                        });
                        try {
                            channel.close();
                        } catch (IOException ignore) {
                        }
                    } else {
                        channel.register(selector, SelectionKey.OP_CONNECT, addr);
                    }
                } catch (IOException e) {
                    Log.d(TAG, "connect channel failed (" + addr.getHostAddress() + ") :" + e.getMessage());
                    try {
                        channel.close();
                    } catch (IOException ignore) {
                    }
                }
            }

            long timeLimit = System.currentTimeMillis() + TIME_OUT;
            for (; ; ) {
                long timeout = timeLimit - System.currentTimeMillis();
                if (mCancel || timeout <= 0) {
                    break;
                }
                int n;
                try {
                    n = selector.select(timeout);
                } catch (IOException e) {
                    break;
                }
                if (n <= 0)
                    break;

                for (SelectionKey key : selector.selectedKeys()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    if (key.isConnectable()) {
                        try {
                            if (channel.finishConnect()) {
                                InetAddress addr = (InetAddress) key.attachment();
                                final String address = addr.getHostAddress();
                                mMainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!mCancel)
                                            mCallback.onScanSuccess(address);
                                    }
                                });
                            }
                        } catch (IOException failed) {
                            //connect failed
                        }
                    }
                    try {
                        channel.close();
                    } catch (IOException e) {
                        Log.d(TAG, "channel close failed:" + e.getMessage());
                    }
                    key.cancel();
                }
            }

            //canceled or timeout or finished, close sockets leftover.
            for (SelectionKey key : selector.keys()) {
                SocketChannel channel = (SocketChannel) key.channel();
                try {
                    channel.close();
                } catch (IOException e) {
                    Log.d(TAG, "channel close failed:" + e.getMessage());
                }
            }

            try {
                selector.close();
            } catch (IOException e) {
                Log.d(TAG, "selector close failed:" + e.getMessage());
            }
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!mCancel)
                        mCallback.onFinish(true);
                }
            });
        }
    }
}
