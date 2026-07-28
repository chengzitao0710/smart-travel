package com.smarttravel.scenic.controller;

import com.smarttravel.common.dto.Result;
import com.smarttravel.scenic.service.IScenicImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Tag(name = "景点图片管理")
@RequestMapping("/scenic-image")
@RestController
public class ScenicImageController {

    @Resource
    private IScenicImageService scenicImageService;

    @Operation(summary = "批量上传景点图片")
    @PostMapping("/batch")
    public Result batchUploadImages(
            @Parameter(description = "景点ID") @RequestParam("scenicId") Long scenicId,
            @Parameter(description = "图片文件列表") @RequestParam("files") List<MultipartFile> files) {
        log.debug("批量上传景点图片，景点ID：{}，文件数量：{}", scenicId, files != null ? files.size() : 0);
        return scenicImageService.batchUploadImages(scenicId, files);
    }

    @Operation(summary = "批量删除景点图片")
    @DeleteMapping("/batch")
    public Result batchDeleteImage(
            @Parameter(description = "图片ID列表") @RequestParam("ids") List<Long> ids) {
        log.debug("批量删除景点图片，ID：{}", ids);
        return scenicImageService.batchDeleteImage(ids);
    }

    @Operation(summary = "查询景点图片")
    @GetMapping("/{scenicId}")
    public Result getImages(
            @Parameter(description = "景点ID") @PathVariable("scenicId") Long scenicId) {
        log.debug("查询景点图片，景点ID：{}", scenicId);
        return scenicImageService.getImages(scenicId);
    }
}