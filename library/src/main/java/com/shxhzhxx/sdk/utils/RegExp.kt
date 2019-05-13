package com.shxhzhxx.sdk.utils

import java.util.regex.Pattern

private const val REGEX_MOBILE = "^[1][3-9][0-9]{9}$"
private val PATTERN_MOBILE by lazy { Pattern.compile(REGEX_MOBILE) }

private const val REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"
private val PATTERN_EMAIL by lazy { Pattern.compile(REGEX_EMAIL) }

@Deprecated("The range of mobile phone numbers is variable and its format varies from country to country.")
fun isMobile(mobile: String) = PATTERN_MOBILE.matcher(mobile).matches()

fun isEmail(email: String) = PATTERN_EMAIL.matcher(email).matches()
