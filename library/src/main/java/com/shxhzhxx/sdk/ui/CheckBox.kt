package com.shxhzhxx.sdk.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.widget.CompoundButtonCompat
import com.shxhzhxx.sdk.R

class CheckBox @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                         defStyleAttr: Int = R.attr.checkboxStyle) : AppCompatCheckBox(context, attrs, defStyleAttr) {
    init {
        val primaryColor = TypedValue().also { getContext().theme.resolveAttribute(R.attr.colorPrimary, it, true) }.data
        val array = context.theme.obtainStyledAttributes(attrs, R.styleable.CheckBox, 0, 0)
        val (colorChecked, colorNormal) = try {
            array.getColor(R.styleable.CheckBox_checkBoxCheckedColor, primaryColor) to array.getColor(R.styleable.CheckBox_checkBoxColor, Color.GRAY)
        } catch (ignore: UnsupportedOperationException) {
            primaryColor to Color.GRAY
        } finally {
            array.recycle()
        }
        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val colors = intArrayOf(colorChecked, colorNormal)
        CompoundButtonCompat.setButtonTintList(this, ColorStateList(states, colors))
    }
}
