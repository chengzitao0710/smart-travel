package com.smarttravel.social.controller;

import com.smarttravel.common.dto.Result;
import com.smarttravel.social.service.IFollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "关注管理")
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService;

    @Operation(summary = "关注/取消关注用户")
    @PutMapping("/{id}/{isFollow}")
    public Result follow(
            @Parameter(description = "被关注用户ID") @PathVariable("id") Long followUserId,
            @Parameter(description = "是否关注") @PathVariable("isFollow") Boolean isFollow) {
        log.info("关注用户: {}, 是否关注: {}", followUserId, isFollow);
        return followService.follow(followUserId, isFollow);
    }

    @Operation(summary = "判断是否关注用户")
    @GetMapping("/or/not/{id}")
    public Result isFollow(
            @Parameter(description = "用户ID") @PathVariable("id") Long followUserId) {
        log.info("是否关注用户: {}", followUserId);
        return followService.isFollow(followUserId);
    }

    @Operation(summary = "获取共同关注")
    @GetMapping("/common/{followUserId}")
    public Result getCommonFollow(
            @Parameter(description = "用户ID") @PathVariable("followUserId") Long followUserId) {
        log.info("获取共同关注用户: {}", followUserId);
        return followService.getCommonFollow(followUserId);
    }

    @Operation(summary = "获取粉丝列表")
    @GetMapping("/followers/{userId}")
    public Result getFollowers(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId) {
        log.info("获取粉丝列表: {}", userId);
        return followService.getFollowers(userId);
    }

    @Operation(summary = "获取关注列表")
    @GetMapping("/followings/{userId}")
    public Result getFollowings(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId) {
        log.info("获取关注列表: {}", userId);
        return followService.getFollowings(userId);
    }

    @Operation(summary = "获取关注/粉丝计数")
    @GetMapping("/counts/{userId}")
    public Result getFollowCounts(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId) {
        log.info("获取关注/粉丝计数: {}", userId);
        return followService.getFollowCounts(userId);
    }
}