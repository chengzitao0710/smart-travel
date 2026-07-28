package com.smarttravel.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smarttravel.common.dto.LoginFormDTO;
import com.smarttravel.common.dto.Result;
import com.smarttravel.user.entity.Traveler;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ITravelerService extends IService<Traveler> {
    Result sendCode(String phone, HttpSession session);
    Result loginByCode(LoginFormDTO loginForm, HttpSession session);
    Result loginByPassword(LoginFormDTO loginForm, HttpSession session);
    Result logout(String token);
    Result getCurrentUser();
    Result getUserById(Long id);
    Result getUserInfo(Long id);
    Result sign();
    Result signCount();
    Result getMonthSignRecord();
    Result signConsecutive();
    Result changePassword(String oldPassword, String newPassword);
    Result updateTravelerInfo(Map<String, Object> params);
    Result uploadAvatar(MultipartFile file);
    Result deleteAvatar();
}