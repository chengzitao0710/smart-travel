package com.smarttravel.common.controller;

import com.smarttravel.common.dto.Result;
import com.smarttravel.common.utils.OssUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Tag(name = "文件上传")
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Resource
    private OssUtils ossUtil;

    @Operation(summary = "上传单张图片")
    @PostMapping("/image")
    public Result uploadImage(
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }
        String url = ossUtil.upload(file);
        log.debug("upload success: {}", url);
        return Result.ok(url);
    }

    @Operation(summary = "批量上传图片")
    @PostMapping("/batch")
    public Result uploadBatch(
            @Parameter(description = "图片文件列表") @RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Result.fail("文件列表不能为空");
        }
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                urls.add(ossUtil.upload(file));
            }
        }
        log.debug("batch upload success, count: {}", urls.size());
        return Result.ok(urls);
    }
}