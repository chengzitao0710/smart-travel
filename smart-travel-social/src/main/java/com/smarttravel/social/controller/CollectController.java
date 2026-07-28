package com.smarttravel.social.controller;

import com.smarttravel.common.dto.Result;
import com.smarttravel.social.service.ICollectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "收藏管理")
@RestController
@RequestMapping("/collect")
public class CollectController {

    @Resource
    private ICollectService collectService;

    @Operation(summary = "收藏目标")
    @PostMapping
    public Result collect(
            @Parameter(description = "目标ID") @RequestParam("targetId") Long targetId,
            @Parameter(description = "目标类型 1=景点 2=游记 3=路线") @RequestParam("targetType") Integer targetType) {
        log.info("collect targetId: {}, targetType: {}", targetId, targetType);
        return collectService.collect(targetId, targetType);
    }

    @Operation(summary = "判断是否已收藏")
    @GetMapping("/or/not")
    public Result isCollect(
            @Parameter(description = "目标ID") @RequestParam("targetId") Long targetId,
            @Parameter(description = "目标类型") @RequestParam("targetType") Integer targetType) {
        log.info("isCollect targetId: {}, targetType: {}", targetId, targetType);
        return collectService.isCollect(targetId, targetType);
    }

    @Operation(summary = "获取我的收藏列表")
    @GetMapping("/my")
    public Result getCollections(
            @Parameter(description = "目标类型(可选)") @RequestParam(value = "targetType", required = false) Integer targetType,
            @Parameter(description = "当前页") @RequestParam(value = "current", defaultValue = "1") Integer current) {
        log.info("getCollections targetType: {}, current: {}", targetType, current);
        return collectService.getCollections(targetType, current);
    }
}