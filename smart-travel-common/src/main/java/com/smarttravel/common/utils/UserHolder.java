package com.smarttravel.common.utils;

import com.smarttravel.common.dto.UserDTO;

public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void setUser(UserDTO user) {
        tl.set(user);
    }

    public static UserDTO getUser() {
        return tl.get();
    }

    public static void removeUser() {
        tl.remove();
    }
}
