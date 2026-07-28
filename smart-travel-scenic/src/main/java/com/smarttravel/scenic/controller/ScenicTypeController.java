package com.smarttravel.scenic.controller;

import com.smarttravel.common.dto.Result;
import com.smarttravel.scenic.service.IScenicTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "景点类型管理")
@RestController
@RequestMapping("/scenic-type")
public class ScenicTypeController {

    @Resource
    private IScenicTypeService scenicTypeService;

    @Operation(summary = "获取所有景点类型")
    @GetMapping("/list")
    public Result getAllList() {
        log.debug("获取所有景点类型");
        return scenicTypeService.getAllTypes();
    }
}