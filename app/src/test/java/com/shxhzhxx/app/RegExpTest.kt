package com.shxhzhxx.app

import com.shxhzhxx.sdk.utils.isMobile
import org.junit.Test

class RegExpTest {
    @Test
    fun test() {
        assert(isMobile("15102154527"))
        assert(!isMobile("151021545237"))
        assert(!isMobile("1510215527"))
    }
}