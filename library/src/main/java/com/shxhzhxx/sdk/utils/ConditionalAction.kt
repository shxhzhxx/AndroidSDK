package com.shxhzhxx.sdk.utils

class ConditionalAction(conditions: Iterable<String>,
                        private val action: ConditionalAction.(invoker: String) -> Unit,
                        private val undo: (ConditionalAction.(invoker: String) -> Unit)? = null) {
    private val conditions = conditions.associate { it to false }.toMutableMap()

    operator fun set(key: String, value: Boolean) {
        if (!conditions.containsKey(key))
            return
        conditions[key] = value
        if (conditions.all { it.value })
            action(key)
        else
            undo?.invoke(this, key)
    }

    fun reset() {
        for (key in conditions.keys) {
            conditions[key] = false
        }
    }
}
