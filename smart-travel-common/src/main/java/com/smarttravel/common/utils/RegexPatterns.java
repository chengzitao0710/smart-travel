package com.smarttravel.common.utils;

public class RegexPatterns {
    public static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9_-]{2,6}+)+$";
    public static final String PASSWORD_REGEX = "^\\w{4,32}$";
}
