package com.shxhzhxx.sdk.utils

import java.util.regex.Pattern

const val REGEX_MOBILE = "^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\\\d{8}\$"
private val PATTERN_MOBILE by lazy { Pattern.compile(REGEX_MOBILE) }

const val REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"
private val PATTERN_EMAIL by lazy { Pattern.compile(REGEX_EMAIL) }

@Deprecated("The range of mobile phone numbers is variable and its format varies from country to country.")
fun isMobile(mobile: String): Boolean {
    return PATTERN_MOBILE.matcher(mobile).matches()
}

fun isEmail(email: String): Boolean {
    return PATTERN_EMAIL.matcher(email).matches()
}
