package com.shxhzhxx.sdk.utils;

import java.util.regex.Pattern;

public abstract class RegEx {
    public static final String REGEX_MOBILE = "^((13[0-9])|(15[^4])|(18[0-9])|(17[0-9])|(147))\\d{8}$";
    private static Pattern PATTERN_MOBILE;
    @Deprecated
    public static boolean isMobile(String mobile) {
        if(PATTERN_MOBILE==null){
            synchronized (RegEx.class){
                if(PATTERN_MOBILE==null){
                    PATTERN_MOBILE=Pattern.compile(REGEX_MOBILE);
                }
            }
        }
        return PATTERN_MOBILE.matcher(mobile).matches();
    }


    public static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
    private static Pattern PATTERN_EMAIL;
    public static boolean isEmail(String email) {
        if(PATTERN_EMAIL==null){
            synchronized (RegEx.class){
                if(PATTERN_EMAIL==null){
                    PATTERN_EMAIL=Pattern.compile(REGEX_EMAIL);
                }
            }
        }
        return PATTERN_EMAIL.matcher(email).matches();
    }
}
