package com.shxhzhxx.sdk.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Build
import android.util.DisplayMetrics

private lateinit var res: Resources
private lateinit var displayMetrics: DisplayMetrics
private lateinit var theme: Resources.Theme

fun initRes(context: Context) {
    res = context.resources
    displayMetrics = context.resources.displayMetrics
    theme = context.theme
}

val statusBarHeight: Int
    get() {
        val resourceId = res.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId != 0) {
            res.getDimensionPixelSize(resourceId)
        } else dpToPx(24f)
    }


fun getString(id: Int, vararg formatArgs: Any) = res.getString(id, formatArgs)

fun getColor(id: Int) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    res.getColor(id, theme)
} else {
    res.getColor(id)
}

fun getBitmap(id: Int) = BitmapFactory.decodeResource(res, id)

fun getDimension(id: Int) = res.getDimension(id)

fun dpToPx(dp: Float) = (dp * displayMetrics.density + 0.5f).toInt()

fun spToPx(sp: Float) = (sp * displayMetrics.scaledDensity + 0.5f).toInt()
