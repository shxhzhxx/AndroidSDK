package com.shxhzhxx.sdk.utils

class ConditionalAction(conditions: List<String>,
                        private val undo: (ConditionalAction.(invoker: String) -> Unit)? = null,
                        private val action: ConditionalAction.(invoker: String) -> Unit) {
    private val conditions = conditions.associate { it to false }.toMutableMap()

    operator fun set(key: String, value: Boolean) {
        if (!conditions.containsKey(key))
            return
        val before = conditions.all { it.value }
        conditions[key] = value
        if (conditions.all { it.value })
            action(key)
        else if (before)
            undo?.invoke(this, key)
    }

    fun reset() {
        for (key in conditions.keys) {
            conditions[key] = false
        }
    }
}
