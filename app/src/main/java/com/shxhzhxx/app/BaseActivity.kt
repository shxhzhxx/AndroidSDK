package com.shxhzhxx.app

import android.os.Bundle
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.shxhzhxx.sdk.activity.DownloadActivity
import com.shxhzhxx.sdk.net
import com.shxhzhxx.sdk.network.PostType
import org.json.JSONObject

abstract class BaseActivity : DownloadActivity() {
    protected inline fun <reified T> post(url: String, params: JSONObject? = null, type: JavaType = TypeFactory.defaultInstance().constructType(T::class.java),
                                          wrap: Boolean = true, postType: PostType = PostType.FORM,
                                          noinline onResponse: ((msg: String, data: T) -> Unit)? = null,
                                          noinline onFailure: ((errno: Int, msg: String, data: String?) -> Unit)? = null): Int {
        return net.post(url, params, type, wrap, this.lifecycle, postType, onResponse, onFailure)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}