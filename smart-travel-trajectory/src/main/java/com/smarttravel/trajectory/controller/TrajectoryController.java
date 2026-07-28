package com.smarttravel.trajectory.controller;

import com.smarttravel.common.dto.Result;
import com.smarttravel.trajectory.service.ITrajectoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "旅行轨迹")
@RestController
@RequestMapping("/trajectory")
public class TrajectoryController {

    @Resource
    private ITrajectoryService trajectoryService;

    @Operation(summary = "打卡景点")
    @PostMapping
    public Result checkIn(
            @Parameter(description = "景点ID") @RequestParam("scenicId") Long scenicId,
            @Parameter(description = "门票订单ID") @RequestParam("ticketId") Long ticketId) {
        log.info("打卡景点: scenicId={}, ticketId={}", scenicId, ticketId);
        return trajectoryService.checkIn(scenicId, ticketId);
    }

    @Operation(summary = "获取我的轨迹")
    @GetMapping("/my")
    public Result getMyTrajectory(
            @Parameter(description = "当前页") @RequestParam(value = "current", defaultValue = "1") Integer current) {
        log.info("获取我的轨迹: current={}", current);
        return trajectoryService.getMyTrajectory(current);
    }

    @Operation(summary = "查看他人轨迹")
    @GetMapping("/user/{userId}")
    public Result getUserTrajectory(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId,
            @Parameter(description = "当前页") @RequestParam(value = "current", defaultValue = "1") Integer current) {
        log.info("查看他人轨迹: userId={}, current={}", userId, current);
        return trajectoryService.getUserTrajectory(userId, current);
    }

    @Operation(summary = "删除打卡记录")
    @DeleteMapping("/{scenicId}")
    public Result deleteCheckIn(
            @Parameter(description = "景点ID") @PathVariable("scenicId") Long scenicId) {
        log.info("删除打卡记录: scenicId={}", scenicId);
        return trajectoryService.deleteCheckIn(scenicId);
    }
}