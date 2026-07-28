package com.smarttravel.travel.controller;

import com.smarttravel.common.dto.Result;
import com.smarttravel.travel.service.INoteImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Tag(name = "游记图片管理")
@RequestMapping("/note-image")
@RestController
public class NoteImageController {

    @Resource
    private INoteImageService noteImageService;

    @Operation(summary = "批量上传游记图片")
    @PostMapping("/{noteId}")
    public Result uploadNoteImageByBatch(
            @Parameter(description = "游记ID") @PathVariable("noteId") Long noteId,
            @Parameter(description = "图片文件列表") @RequestParam("files") List<MultipartFile> files) {
        log.debug("批量上传游记图片: noteId={}, fileCount={}", noteId, files.size());
        return noteImageService.uploadNoteImageByBatch(noteId, files);
    }

    @Operation(summary = "批量删除游记图片")
    @DeleteMapping("/{noteId}")
    public Result deleteNoteImageByBatch(
            @Parameter(description = "游记ID") @PathVariable("noteId") Long noteId,
            @Parameter(description = "图片ID列表") @RequestParam("imageIds") List<Long> imageIds) {
        log.debug("批量删除游记图片: noteId={}, imageIds={}", noteId, imageIds);
        return noteImageService.deleteNoteImageByBatch(noteId, imageIds);
    }

    @Operation(summary = "获取游记图片列表")
    @GetMapping("/{noteId}")
    public Result getImagesByNoteId(
            @Parameter(description = "游记ID") @PathVariable("noteId") Long noteId) {
        log.debug("获取游记图片列表: noteId={}", noteId);
        return noteImageService.getImagesByNoteId(noteId);
    }
}