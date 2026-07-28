package com.smarttravel.user.controller;

import com.smarttravel.common.dto.LoginFormDTO;
import com.smarttravel.common.dto.Result;
import com.smarttravel.user.service.ITravelerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@Slf4j
@Tag(name = "旅行者管理")
@RequestMapping("/traveler")
public class TravelerController {

    @Resource
    private ITravelerService travelerService;

    @Operation(summary = "发送登录验证码")
    @PostMapping("/code")
    public Result sendCode(
            @Parameter(description = "手机号") @RequestParam("phone") String phone,
            HttpSession session) {
        log.debug("sendCode: {}", phone);
        return travelerService.sendCode(phone, session);
    }

    @Operation(summary = "验证码登录")
    @PostMapping("/login/code")
    public Result loginByCode(@RequestBody LoginFormDTO loginForm, HttpSession session) {
        log.debug("loginByCode: {}", loginForm);
        return travelerService.loginByCode(loginForm, session);
    }

    @Operation(summary = "密码登录")
    @PostMapping("/login/password")
    public Result loginByPassword(@RequestBody LoginFormDTO loginForm, HttpSession session) {
        log.debug("loginByPassword: {}", loginForm);
        return travelerService.loginByPassword(loginForm, session);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result logout(
            @Parameter(description = "Bearer Token") @RequestHeader("Authorization") String tokenHeader) {
        log.debug("logout: {}", tokenHeader);
        String token = tokenHeader.replace("Bearer ", "");
        return travelerService.logout(token);
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public Result getCurrentUser() {
        log.debug("getCurrentUser");
        return travelerService.getCurrentUser();
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/info/{id}")
    public Result getUserInfo(
            @Parameter(description = "用户ID") @PathVariable("id") Long id) {
        log.debug("getUserInfo: {}", id);
        return travelerService.getUserInfo(id);
    }

    @Operation(summary = "根据ID获取用户")
    @GetMapping("/{id}")
    public Result getUserById(
            @Parameter(description = "用户ID") @PathVariable("id") Long id) {
        log.debug("getUserById: {}", id);
        return travelerService.getUserById(id);
    }

    @Operation(summary = "签到")
    @PostMapping("/sign")
    public Result sign() {
        log.debug("sign");
        return travelerService.sign();
    }

    @Operation(summary = "获取签到次数")
    @GetMapping("/sign/count")
    public Result signCount() {
        log.debug("signCount");
        return travelerService.signCount();
    }

    @Operation(summary = "获取本月签到状态")
    @GetMapping("/sign/month")
    public Result signMonth() {
        log.debug("getMonthSignRecord");
        return travelerService.getMonthSignRecord();
    }

    @Operation(summary = "获取连续签到天数")
    @GetMapping("/sign/consecutive")
    public Result signConsecutive() {
        log.debug("signConsecutive");
        return travelerService.signConsecutive();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result changePassword(
            @Parameter(description = "旧密码和新密码(oldPassword,newPassword)") @RequestBody Map<String, String> params) {
        log.debug("changePassword: {}", params);
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        return travelerService.changePassword(oldPassword, newPassword);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/info")
    public Result updateTravelerInfo(
            @Parameter(description = "用户信息字段(phone,name等)") @RequestBody Map<String, Object> params) {
        log.debug("updateTravelerInfo: {}", params);
        return travelerService.updateTravelerInfo(params);
    }

    @Operation(summary = "上传用户头像")
    @PostMapping("/avatar")
    public Result uploadAvatar(
            @Parameter(description = "头像文件") @RequestParam("file") MultipartFile file) {
        log.debug("uploadAvatar: {}", file.getOriginalFilename());
        return travelerService.uploadAvatar(file);
    }

    @Operation(summary = "删除用户头像")
    @DeleteMapping("/avatar")
    public Result deleteAvatar() {
        log.debug("deleteAvatar");
        return travelerService.deleteAvatar();
    }
}