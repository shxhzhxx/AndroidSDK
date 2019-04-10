package com.shxhzhxx.sdk.activity

import android.os.Build
import android.os.Bundle
import android.transition.Transition
import android.util.TypedValue
import com.shxhzhxx.sdk.utils.ConditionalAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class SharedElementActivity : DownloadActivity() {
    var runAfterTransition: (() -> Unit)? = null
        set(value) {
            field = value
            if (field != null)
                action["invoke"] = true
        }
    private val action = ConditionalAction(arrayOf("transition", "invoke")) {
        if (!isCancelled)
            runAfterTransition?.invoke()
        runAfterTransition = null
    }

    private var isCancelled = false
    override fun onBackPressed() {
        isCancelled = true
        super.onBackPressed()
    }

    override fun finishAfterTransition() {
        isCancelled = true
        super.finishAfterTransition()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            launch {
                val transition = window.sharedElementEnterTransition
                        ?: run {
                            val duration = TypedValue().also { theme.resolveAttribute(android.R.attr.duration, it, true) }
                                    .data.let { if (it > 0) it else 300 }
                            delay(duration.toLong())
                            action["transition"] = true
                            return@launch
                        }
                transition.addListener(object : Transition.TransitionListener {
                    override fun onTransitionEnd(transition: Transition) {
                        action["transition"] = true
                        transition.removeListener(this)
                    }

                    override fun onTransitionResume(transition: Transition) {
                    }

                    override fun onTransitionPause(transition: Transition) {
                    }

                    override fun onTransitionCancel(transition: Transition) {
                    }

                    override fun onTransitionStart(transition: Transition) {
                    }
                })
            }
        }
    }
}