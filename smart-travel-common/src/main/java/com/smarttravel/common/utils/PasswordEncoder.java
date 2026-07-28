package com.smarttravel.common.utils;


import cn.hutool.crypto.digest.DigestUtil;

public class PasswordEncoder {
    public static String encode(String password) {
        return DigestUtil.md5Hex(password + SystemConstants.PASSWORD_SALT);
    }

    public static boolean matches(String password, String encodedPassword) {
        return encodedPassword.equals(encode(password));
    }
}
