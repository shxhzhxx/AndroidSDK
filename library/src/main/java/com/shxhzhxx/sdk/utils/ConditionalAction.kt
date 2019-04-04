package com.shxhzhxx.sdk.utils

import androidx.annotation.IntRange

class ConditionalAction(@IntRange(from = 1) size: Int, private val action: ConditionalAction.(invoker: Int) -> Unit) {
    private val conditions: BooleanArray = BooleanArray(size)

    operator fun set(index: Int, cond: Boolean) {
        conditions[index] = cond
        if (conditions.all { it })
            action(index)
    }

    fun reset() {
        for (i in conditions.indices) {
            conditions[i] = false
        }
    }
}
