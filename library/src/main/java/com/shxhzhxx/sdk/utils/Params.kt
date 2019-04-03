package com.shxhzhxx.sdk.utils

import android.content.Context
import android.content.SharedPreferences


lateinit var sharedPreferences: SharedPreferences

fun initParams(context: Context) {
    sharedPreferences = context.getSharedPreferences("${context.packageName}_AndroidSdk", Context.MODE_PRIVATE)
}

inline fun <reified T> saveParam(key: Param<T>, value: T) {
    sharedPreferences.edit().apply {
        when (value) {
            is String -> putString(key.name, value)
            is Int -> putInt(key.name, value)
            is Boolean -> putBoolean(key.name, value)
            is Long -> putLong(key.name, value)
            is Float -> putFloat(key.name, value)
            else -> throw IllegalArgumentException("Invalid param type: ${T::class.java.name}")
        }
    }.apply()
}

inline fun <reified T> getParam(key: Param<T>) = sharedPreferences.run {
    return@run when (key.defVal) {
        is String -> getString(key.name, key.defVal) as T
        is Int -> getInt(key.name, key.defVal) as T
        is Boolean -> getBoolean(key.name, key.defVal) as T
        is Long -> getLong(key.name, key.defVal) as T
        is Float -> getFloat(key.name, key.defVal) as T
        else -> throw IllegalArgumentException("Invalid param type: ${T::class.java.name}")
    }
}

class Param<T>(val name: String, val defVal: T)
