package com.shxhzhxx.sdk.utils

import android.content.Context
import android.content.SharedPreferences


lateinit var sharedPreferences: SharedPreferences

fun initParams(context: Context) {
    sharedPreferences = context.getSharedPreferences("${context.packageName}_AndroidSdk", Context.MODE_PRIVATE)
}

fun <T> saveParam(key: Param<T>, value: T) {
    sharedPreferences.edit().apply {
        when (value) {
            is String -> putString(key.name, value)
            is Int -> putInt(key.name, value)
            is Boolean -> putBoolean(key.name, value)
            is Long -> putLong(key.name, value)
            is Float -> putFloat(key.name, value)
            else -> throw IllegalArgumentException("Invalid param type:${key.type}")
        }
    }.apply()
}

inline fun <reified T> getParam(key: Param<T>) = when (key.defVal) {
    is String -> sharedPreferences.getString(key.name, key.defVal) as T
    is Int -> sharedPreferences.getInt(key.name, key.defVal) as T
    is Boolean -> sharedPreferences.getBoolean(key.name, key.defVal) as T
    is Long -> sharedPreferences.getLong(key.name, key.defVal) as T
    is Float -> sharedPreferences.getFloat(key.name, key.defVal) as T
    else -> throw IllegalArgumentException("Invalid param type:${key.type}")
}

class Param<T>(val name: String, val type: Class<T>, val defVal: T)
