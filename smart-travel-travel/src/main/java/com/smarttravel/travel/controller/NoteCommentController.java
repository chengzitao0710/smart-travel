package com.smarttravel.travel.controller;

import com.smarttravel.common.dto.Result;
import com.smarttravel.travel.service.INoteCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "游记评论管理")
@RequestMapping("/note-comment")
@RestController
public class NoteCommentController {

    @Resource
    private INoteCommentService noteCommentService;

    @Operation(summary = "添加评论")
    @PostMapping("/{noteId}")
    public Result addComment(
            @Parameter(description = "游记ID") @PathVariable("noteId") Long noteId,
            @Parameter(description = "评论内容") @RequestParam("content") String content) {
        log.debug("添加评论: noteId={}, content={}", noteId, content);
        return noteCommentService.addComment(noteId, content);
    }

    @Operation(summary = "获取评论列表")
    @GetMapping("/{noteId}")
    public Result getComments(
            @Parameter(description = "游记ID") @PathVariable("noteId") Long noteId,
            @Parameter(description = "当前页") @RequestParam(value = "current", defaultValue = "1") Integer current) {
        log.debug("获取评论列表: noteId={}, current={}", noteId, current);
        return noteCommentService.getComments(noteId, current);
    }

    @Operation(summary = "点赞评论")
    @PutMapping("/like/{id}")
    public Result likeComment(
            @Parameter(description = "评论ID") @PathVariable("id") Long id) {
        log.debug("点赞评论: id={}", id);
        return noteCommentService.likeComment(id);
    }

    @Operation(summary = "回复评论")
    @PostMapping("/reply/{noteId}")
    public Result replyComment(
            @Parameter(description = "游记ID") @PathVariable("noteId") Long noteId,
            @Parameter(description = "父评论ID") @RequestParam("parentId") Long parentId,
            @Parameter(description = "回复目标评论ID") @RequestParam("answerId") Long answerId,
            @Parameter(description = "回复内容") @RequestParam("content") String content) {
        log.debug("回复评论: noteId={}, parentId={}, answerId={}, content={}", noteId, parentId, answerId, content);
        return noteCommentService.replyComment(noteId, parentId, answerId, content);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result deleteComment(
            @Parameter(description = "评论ID") @PathVariable("id") Long id) {
        log.debug("删除评论: id={}", id);
        return noteCommentService.deleteComment(id);
    }

    @Operation(summary = "举报评论")
    @PutMapping("/report/{id}")
    public Result reportComment(
            @Parameter(description = "评论ID") @PathVariable("id") Long id) {
        log.debug("举报评论: id={}", id);
        return noteCommentService.reportComment(id);
    }

    @Operation(summary = "禁用评论")
    @PutMapping("/ban/{id}")
    public Result banComment(
            @Parameter(description = "评论ID") @PathVariable("id") Long id) {
        log.debug("禁用评论: id={}", id);
        return noteCommentService.banComment(id);
    }
}