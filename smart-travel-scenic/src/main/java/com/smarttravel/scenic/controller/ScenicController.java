package com.smarttravel.scenic.controller;

import com.smarttravel.common.dto.Result;
import com.smarttravel.scenic.entity.Scenic;
import com.smarttravel.scenic.service.IScenicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "景点管理")
@RequestMapping("/scenic")
@RestController
public class ScenicController {

    @Resource
    private IScenicService scenicService;

    @Operation(summary = "根据ID查询景点")
    @GetMapping("{id}")
    public Result getScenicById(
            @Parameter(description = "景点ID") @PathVariable("id") Long id) {
        log.debug("根据ID查询景点详情，ID：{}", id);
        return scenicService.getScenicById(id);
    }

    @Operation(summary = "创建景点")
    @PostMapping()
    public Result createScenic(@RequestBody Scenic scenic) {
        log.debug("景点信息，scenic: {}", scenic);
        return scenicService.createScenic(scenic);
    }

    @Operation(summary = "更新景点")
    @PutMapping()
    public Result updateScenic(@RequestBody Scenic scenic) {
        log.debug("更新景点信息，scenic: {}", scenic);
        return scenicService.updateScenic(scenic);
    }

    @Operation(summary = "删除景点")
    @DeleteMapping("{id}")
    public Result deleteScenic(
            @Parameter(description = "景点ID") @PathVariable("id") Long id) {
        log.debug("根据ID删除景点详情，ID：{}", id);
        return scenicService.deleteScenic(id);
    }

    @Operation(summary = "综合搜索景点")
    @GetMapping("/search")
    public Result searchScenic(
            @Parameter(description = "搜索关键词") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "景点类型ID") @RequestParam(value = "typeId", required = false) Long typeId,
            @Parameter(description = "所在区域") @RequestParam(value = "area", required = false) String area,
            @Parameter(description = "经度") @RequestParam(value = "x", required = false) Double x,
            @Parameter(description = "纬度") @RequestParam(value = "y", required = false) Double y,
            @Parameter(description = "排序: hot=热门 top=高分 newest=最新") @RequestParam(value = "sort", defaultValue = "newest") String sort,
            @Parameter(description = "当前页") @RequestParam(value = "current", defaultValue = "1") Integer current) {
        log.debug("综合搜索景点，keyword={}, typeId={}, area={}, x={}, y={}, sort={}, current={}",
                keyword, typeId, area, x, y, sort, current);
        return scenicService.searchScenic(keyword, typeId, area, x, y, sort, current);
    }

    @Operation(summary = "高德POI搜索")
    @GetMapping("/poi")
    public Result searchPoi(
            @Parameter(description = "搜索关键词") @RequestParam("keyword") String keyword) {
        log.debug("高德POI搜索，关键词：{}", keyword);
        return scenicService.searchPoi(keyword);
    }

    @Operation(summary = "切换景点上下架状态")
    @PutMapping("/status/{id}")
    public Result toggleStatus(
            @Parameter(description = "景点ID") @PathVariable("id") Long id,
            @Parameter(description = "状态 1=上架 0=下架") @RequestParam("status") Integer status) {
        log.debug("切换景点上下架状态，ID：{}，状态：{}", id, status);
        return scenicService.toggleStatus(id, status);
    }

    @Operation(summary = "全量同步景点坐标到Redis GEO")
    @PostMapping("/geo/sync")
    public Result syncAllScenicGeo() {
        log.debug("全量同步景点坐标到 Redis GEO");
        return scenicService.syncAllScenicGeo();
    }
}