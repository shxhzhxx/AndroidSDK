package com.shxhzhxx.sdk.utils

import java.io.File

infix fun File.copyTo(dst: File): Boolean {
    return try {
        inputStream().channel.use { input ->
            dst.outputStream().channel.use { output ->
                output.transferFrom(input, 0, input.size())
            }
        }
        true
    } catch (e: Throwable) {
        false
    }
}
