package com.shxhzhxx.sdk

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

open class CoroutineFragment : Fragment(), CoroutineScope {
    class MyViewModel : ViewModel() {
        private val job = SupervisorJob()
        val coroutineContext: CoroutineContext
            get() = Dispatchers.Main + job

        override fun onCleared() {
            super.onCleared()
            job.cancel()
        }
    }
    override val coroutineContext: CoroutineContext
        get() = vm.coroutineContext
    private val vm by lazy { ViewModelProviders.of(this).get(MyViewModel::class.java) }
}
