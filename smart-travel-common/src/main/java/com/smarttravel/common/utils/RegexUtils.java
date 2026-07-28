package com.smarttravel.common.utils;

import cn.hutool.core.util.ReUtil;

public class RegexUtils {
    public static boolean isPhoneInvalid(String phone) {
        return !ReUtil.isMatch(RegexPatterns.PHONE_REGEX, phone);
    }

    public static boolean isEmailInvalid(String email) {
        return !ReUtil.isMatch(RegexPatterns.EMAIL_REGEX, email);
    }

    public static boolean isPasswordInvalid(String password) {
        return !ReUtil.isMatch(RegexPatterns.PASSWORD_REGEX, password);
    }
}