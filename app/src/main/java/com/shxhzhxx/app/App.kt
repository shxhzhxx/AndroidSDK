package com.shxhzhxx.app

import com.shxhzhxx.sdk.Application
import com.shxhzhxx.sdk.OnProcessCreate

class App : Application() {

    @OnProcessCreate(["bbbb"])
    fun onSubProcessCreate() {

    }
}
