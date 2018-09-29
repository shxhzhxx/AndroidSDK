package com.shxhzhxx.sdk.network;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.shxhzhxx.sdk.R;
import com.shxhzhxx.urlloader.MultiObserverTaskManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Net extends MultiObserverTaskManager<Net.NetListener> {
    private static final String TAG = "Net";
    public static final int CODE_OK = 0;
    public static final int CODE_NO_AVAILABLE_NETWORK = -1;
    public static final int CODE_TIMEOUT = -2;
    public static final int CODE_GET_PARAM_FAILED = -3;
    public static final int CODE_UNEXPECTED_RESPONSE = -4;
    public static final int CODE_CANCELED = -5;

    public static final MediaType DATA_TYPE_FORM = MediaType.parse("application/x-www-form-urlencoded;charset=utf-8");//form表单
    public static final MediaType DATA_TYPE_FILE = MediaType.parse("application/octet-stream");
    public static final MediaType DATA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8");

    public interface NetListener {
        JSONObject getParam() throws JSONException;

        /**
         * this callback will always been invoke after the task has finished.
         * check the errno to determine how to handle the result.
         */
        void onResult(int errno, String msg, String data);
    }

    public static class NetListenerAdapter implements NetListener {
        public JSONObject getParam() throws JSONException {
            return new JSONObject();
        }

        public void onResult(int errno, String msg, String data) {
        }
    }

    private static volatile Net mInstance;

    public static synchronized void init(@NonNull Context context) {
        if (mInstance == null)
            mInstance = new Net(context);
    }

    public static Net getInstance() {
        return mInstance;
    }


    private ConnectivityManager mConnMgr;
    private OkHttpClient mOkHttpClient;
    private SparseArray<String> mCodeMessage;
    private String DEFAULT_ERROR_MESSAGE;

    public Net(Context context) {
        super(5);
        mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mCodeMessage = new SparseArray<>();
        mOkHttpClient = new OkHttpClient.Builder()
                .writeTimeout(3, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .build();

        Resources resources = context.getApplicationContext().getResources();
        DEFAULT_ERROR_MESSAGE = resources.getString(R.string.err_msg_default);
        mCodeMessage.put(CODE_OK, resources.getString(R.string.err_msg_ok));
        mCodeMessage.put(CODE_NO_AVAILABLE_NETWORK, resources.getString(R.string.err_msg_no_available_network));
        mCodeMessage.put(CODE_TIMEOUT, resources.getString(R.string.err_msg_timeout));
        mCodeMessage.put(CODE_GET_PARAM_FAILED, resources.getString(R.string.err_msg_get_param_failed));
        mCodeMessage.put(CODE_UNEXPECTED_RESPONSE, resources.getString(R.string.err_msg_unexpected_response));
        mCodeMessage.put(CODE_CANCELED, resources.getString(R.string.err_msg_cancelled));
    }

    public String getMsg(int errno) {
        return getMsg(errno, DEFAULT_ERROR_MESSAGE);
    }

    public String getMsg(int errno, String defValue) {
        return mCodeMessage.get(errno, defValue);
    }

    public int executeRequest(final String key, String tag, final Request request, NetListener listener) {
        cancel(key);
        return start(key, tag, listener, new TaskBuilder() {
            @Override
            public Task build() {
                return new RequestTask(key, request);
            }
        });
    }

    public int postForm(String url, String key, @Nullable String tag, NetListener listener) {
        if (listener == null || TextUtils.isEmpty(url) || TextUtils.isEmpty(key)) {
            Log.e(key, "Net.postForm: invalid params");
            if (listener != null)
                listener.onResult(Net.CODE_GET_PARAM_FAILED, getMsg(Net.CODE_GET_PARAM_FAILED), null);
            return -1;
        }
        JSONObject requestJson;
        try {
            requestJson = listener.getParam();
        } catch (JSONException e) {
            listener.onResult(CODE_GET_PARAM_FAILED, mCodeMessage.get(CODE_GET_PARAM_FAILED), null);
            return -1;
        }
        RequestBody body = RequestBody.create(DATA_TYPE_FORM, formatJsonToFormData(requestJson));
        Request request = new Request.Builder().url(url).post(body).build();
        return executeRequest(key, tag, request, listener);
    }

    public int postMultipartForm(String url, String key, @Nullable String tag, String fileKey, File file, NetListener listener) {
        if (listener == null || file == null || !file.exists() || TextUtils.isEmpty(key) || TextUtils.isEmpty(url)) {
            Log.e(key, "postMultipartForm: invalid params");
            if (listener != null)
                listener.onResult(Net.CODE_GET_PARAM_FAILED, getMsg(Net.CODE_GET_PARAM_FAILED), null);
            return -1;
        }
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(fileKey, file.getName(), RequestBody.create(DATA_TYPE_FILE, file));
        try {
            JSONObject requestJson = listener.getParam();
            Iterator<String> iterator = requestJson.keys();
            while (iterator.hasNext()) {
                String k = iterator.next();
                String v = requestJson.getString(k);
                builder.addFormDataPart(k, v);
            }
        } catch (JSONException ignore) {
        }

        Request request = new Request.Builder()
                .url(url)
                .post(builder.build()).build();
        return executeRequest(key, tag, request, listener);
    }

    public int postFile(String url, String key, @Nullable String tag, File file, NetListener listener) {
        if (listener == null || file == null || !file.exists() || TextUtils.isEmpty(key) || TextUtils.isEmpty(url)) {
            Log.e(key, "postFile: invalid params");
            if (listener != null)
                listener.onResult(Net.CODE_GET_PARAM_FAILED, getMsg(Net.CODE_GET_PARAM_FAILED), null);
            return -1;
        }
        Request request = new Request.Builder().url(url)
                .post(RequestBody.create(DATA_TYPE_FILE, file))
                .build();
        return executeRequest(key, tag, request, listener);
    }

    public int postJson(String url, String key, @Nullable String tag, NetListener listener) {
        if (listener == null || TextUtils.isEmpty(url) || TextUtils.isEmpty(key)) {
            Log.e(key, "postJson: invalid params");
            if (listener != null)
                listener.onResult(Net.CODE_GET_PARAM_FAILED, getMsg(Net.CODE_GET_PARAM_FAILED), null);
            return -1;
        }
        Request request;
        try {
            request = new Request.Builder().url(url)
                    .post(RequestBody.create(DATA_TYPE_JSON, listener.getParam().toString()))
                    .build();
        } catch (JSONException e) {
            listener.onResult(CODE_GET_PARAM_FAILED, mCodeMessage.get(CODE_GET_PARAM_FAILED), null);
            return -1;
        }
        return executeRequest(key, tag, request, listener);
    }

    private class RequestTask extends Task {
        private Request mRequest;

        public RequestTask(String key, Request request) {
            super(key);
            mRequest = request;
        }

        @Override
        protected void onCanceled() {
            super.onCanceled();
            for (NetListener listener : getObservers()) {
                if (listener != null) {
                    listener.onResult(CODE_CANCELED, getMsg(CODE_CANCELED), null);
                }
            }
        }

        @Override
        protected void onObserverUnregistered(NetListener observer) {
            if (observer != null)
                observer.onResult(CODE_CANCELED, getMsg(CODE_CANCELED), null);
        }

        @Override
        protected void doInBackground() {
            Call call = mOkHttpClient.newCall(mRequest);
            Response response;
            try {
                response = call.execute();
            } catch (IOException e) {
                Log.e(TAG, "execute IOException: " + e.getMessage());
                setPostResult(new Runnable() {
                    @Override
                    public void run() {
                        int errno = isNetworkAvailable() ? CODE_TIMEOUT : CODE_NO_AVAILABLE_NETWORK;
                        for (NetListener listener : getObservers()) {
                            if (listener != null)
                                listener.onResult(errno, getMsg(errno), null);
                        }
                    }
                });
                return;
            }
            if (!response.isSuccessful()) {
                response.close();
                Log.e(TAG, "HTTP code " + response.code());
                setPostResult(new Runnable() {
                    @Override
                    public void run() {
                        for (NetListener listener : getObservers()) {
                            if (listener != null)
                                listener.onResult(CODE_UNEXPECTED_RESPONSE, getMsg(CODE_UNEXPECTED_RESPONSE), null);
                        }
                    }
                });
                return;
            }
            ResponseBody body = response.body();
            assert body != null;
            final String data;
            try {
                data = body.string();
            } catch (IOException e) {
                Log.e(TAG, "string IOException: " + e.getMessage());
                body.close();
                setPostResult(new Runnable() {
                    @Override
                    public void run() {
                        int errno = isNetworkAvailable() ? CODE_UNEXPECTED_RESPONSE : CODE_NO_AVAILABLE_NETWORK;
                        for (NetListener listener : getObservers()) {
                            if (listener != null)
                                listener.onResult(errno, getMsg(errno), null);
                        }
                    }
                });
                return;
            }
            setPostResult(new Runnable() {
                @Override
                public void run() {
                    for (NetListener listener : getObservers()) {
                        if (listener != null)
                            listener.onResult(CODE_OK, getMsg(CODE_OK), data);
                    }
                }
            });
        }
    }

    public boolean isNetworkAvailable() {
        NetworkInfo networkInfo = mConnMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public boolean isWifiAvailable() {
        NetworkInfo networkInfo = mConnMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 将json格式的请求参数转化成表单格式提交，转化过程中可能对特殊字符转码
     * param json {"k1":"v1","k2":"v2","k3":{"k4":"v4","k5":"v5"}}
     * return k1=v1&k2=v2&k3={"k4":"v4","k5":"v5"}
     */
    public static String formatJsonToFormData(JSONObject json) {
        List<String> list = new ArrayList<>();
        Iterator<String> iterator = json.keys();
        while (iterator.hasNext()) {
            try {
                String key = iterator.next();
                String value = json.getString(key);
                list.add(String.format(Locale.CHINA, "%s=%s", URLEncoder.encode(key, "UTF-8"), URLEncoder.encode(value, "UTF-8")));
            } catch (JSONException e) {
                Log.e("formatJsonToFromData", "unexpected JSONException:" + e.getMessage());
            } catch (UnsupportedEncodingException e) {
                Log.e("formatJsonToFromData", "unexpected UnsupportedEncodingException:" + e.getMessage());
            }
        }
        return TextUtils.join("&", list);
    }

    /**
     * 将json格式的字符串，加入分行缩进字符，方便格式化输出。
     * formatJsonString是费时操作，应通过len_limit限制被处理数据的长度
     */
    public static String formatJsonString(String raw, int len_limit) {
        if (TextUtils.isEmpty(raw) || len_limit < 0)
            return "";
        raw = raw.replaceAll("\\s*", "");
        StringBuilder result = new StringBuilder();
        String row_head = "";
        String tab_space = "   ";
        int max_index = Math.min(raw.length(), len_limit);
        boolean quotation = false;
        for (int i = 0; i < max_index; ++i) {
            if (quotation && !raw.substring(i, i + 1).equals("\"")) {
                result.append(raw.substring(i, i + 1));
                continue;
            }
            switch (raw.substring(i, i + 1)) {
                case "\"":
                    quotation = !quotation;
                    result.append(raw.substring(i, i + 1));
                    break;
                case "{":
                    row_head += tab_space;
                    result.append("{\n").append(row_head);
                    break;
                case "}":
                    row_head = row_head.replaceFirst(tab_space, "");
                    result.append("\n").append(row_head).append("}");
                    break;
                case ",":
                    result.append("," + "\n").append(row_head);
                    break;
                case "[":
                    row_head += tab_space;
                    result.append("[\n").append(row_head);
                    break;
                case "]":
                    row_head = row_head.replaceFirst(tab_space, "");
                    result.append("\n").append(row_head).append("]");
                    break;
                default:
                    result.append(raw.substring(i, i + 1));
            }
        }
        if (raw.length() > len_limit) {
            result.replace(Math.max(0, result.lastIndexOf(tab_space)), result.length(), tab_space + "......");
        }
        return result.toString();
    }
}
