package com.smarttravel.route.controller;

import com.smarttravel.common.dto.Result;
import com.smarttravel.route.entity.Route;
import com.smarttravel.route.entity.RouteDetail;
import com.smarttravel.route.service.IRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Tag(name = "路线管理")
@RestController
@RequestMapping("/route")
public class RouteController {

    @Resource
    private IRouteService routeService;

    @Operation(summary = "创建路线")
    @PostMapping
    public Result createRoute(@RequestBody Route route) {
        log.info("创建路线: {}", route.getTitle());
        return routeService.createRoute(route);
    }

    @Operation(summary = "更新路线")
    @PutMapping
    public Result updateRoute(@RequestBody Route route) {
        log.info("更新路线: {}", route.getId());
        return routeService.updateRoute(route);
    }

    @Operation(summary = "获取路线详情")
    @GetMapping("/{id}")
    public Result getRouteById(
            @Parameter(description = "路线ID") @PathVariable("id") Long id) {
        log.info("获取路线详情: {}", id);
        return routeService.getRouteById(id);
    }

    @Operation(summary = "查询路线列表")
    @GetMapping("/list")
    public Result getRouteList(
            @Parameter(description = "当前页") @RequestParam(value = "current", defaultValue = "1") Integer current,
            @Parameter(description = "难度等级(可选)") @RequestParam(value = "difficulty", required = false) Integer difficulty,
            @Parameter(description = "城市名称(可选)") @RequestParam(value = "city", required = false) String city) {
        log.info("查询路线列表: current={}, difficulty={}, city={}", current, difficulty, city);
        return routeService.getRouteList(current, difficulty, city);
    }

    @Operation(summary = "获取路线行程")
    @GetMapping("/{routeId}/details")
    public Result getRouteDetails(
            @Parameter(description = "路线ID") @PathVariable("routeId") Long routeId) {
        log.info("获取路线行程: {}", routeId);
        return routeService.getRouteDetails(routeId);
    }

    @Operation(summary = "删除路线")
    @DeleteMapping("/{id}")
    public Result deleteRoute(
            @Parameter(description = "路线ID") @PathVariable("id") Long id) {
        log.info("删除路线: {}", id);
        return routeService.deleteRoute(id);
    }

    @Operation(summary = "计算路线导航")
    @GetMapping("/{routeId}/direction")
    public Result calcDirection(
            @Parameter(description = "路线ID") @PathVariable("routeId") Long routeId) {
        log.info("计算路线导航: {}", routeId);
        return routeService.calcDirection(routeId);
    }

    @Operation(summary = "获取热门路线")
    @GetMapping("/hot")
    public Result getHotRoutes() {
        log.info("获取热门路线");
        return routeService.getHotRoutes();
    }

    @Operation(summary = "获取我的路线")
    @GetMapping("/my")
    public Result getMyRoutes(
            @Parameter(description = "当前页") @RequestParam(value = "current", defaultValue = "1") Integer current) {
        log.info("获取我的路线: current={}", current);
        return routeService.getMyRoutes(current);
    }

    @Operation(summary = "批量保存路线行程")
    @PostMapping("/{routeId}/details")
    public Result saveRouteDetails(
            @Parameter(description = "路线ID") @PathVariable("routeId") Long routeId,
            @Parameter(description = "行程列表") @RequestBody List<RouteDetail> details) {
        log.info("保存路线行程: routeId={}, count={}", routeId, details.size());
        return routeService.saveRouteDetails(routeId, details);
    }

    @Operation(summary = "更新单条路线行程")
    @PutMapping("/{routeId}/details")
    public Result updateRouteDetail(
            @Parameter(description = "路线ID") @PathVariable("routeId") Long routeId,
            @Parameter(description = "行程信息") @RequestBody RouteDetail detail) {
        log.info("更新路线行程: routeId={}, detailId={}", routeId, detail.getId());
        return routeService.updateRouteDetail(detail);
    }

    @Operation(summary = "删除单条路线行程")
    @DeleteMapping("/{routeId}/details/{detailId}")
    public Result deleteRouteDetail(
            @Parameter(description = "路线ID") @PathVariable("routeId") Long routeId,
            @Parameter(description = "行程ID") @PathVariable("detailId") Long detailId) {
        log.info("删除路线行程: routeId={}, detailId={}", routeId, detailId);
        return routeService.deleteRouteDetail(detailId);
    }

    @Operation(summary = "上传路线封面图")
    @PostMapping("/{routeId}/cover")
    public Result uploadCover(
            @Parameter(description = "路线ID") @PathVariable("routeId") Long routeId,
            @Parameter(description = "封面图文件") @RequestParam("file") MultipartFile file) {
        log.info("上传路线封面: routeId={}", routeId);
        return routeService.uploadCover(routeId, file);
    }

    @Operation(summary = "设置路线热门状态")
    @PutMapping("/{routeId}/hot")
    public Result setRouteHot(
            @Parameter(description = "路线ID") @PathVariable("routeId") Long routeId,
            @Parameter(description = "是否热门 0=否 1=是") @RequestParam("isHot") Integer isHot) {
        log.info("设置路线热门: routeId={}, isHot={}", routeId, isHot);
        return routeService.setRouteHot(routeId, isHot);
    }
}