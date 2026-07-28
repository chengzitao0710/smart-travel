package com.smarttravel.travel.controller;

import com.smarttravel.common.dto.Result;
import com.smarttravel.travel.entity.TravelNote;
import com.smarttravel.travel.service.ITravelNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@Tag(name = "游记管理")
@RequestMapping("/travel-note")
@RestController
public class TravelNoteController {

    @Resource
    private ITravelNoteService travelNoteService;

    @Operation(summary = "发布游记")
    @PostMapping("/publish")
    public Result publishNote(@RequestBody TravelNote travelNote) {
        log.debug("发布游记: {}", travelNote);
        return travelNoteService.publishNote(travelNote);
    }

    @Operation(summary = "保存草稿")
    @PostMapping("/draft")
    public Result saveDraft(@RequestBody TravelNote travelNote) {
        log.debug("保存草稿: {}", travelNote);
        return travelNoteService.saveDraft(travelNote);
    }

    @Operation(summary = "发布草稿")
    @PutMapping("/draft/{id}")
    public Result publishDraft(
            @Parameter(description = "游记ID") @PathVariable("id") Long id) {
        log.debug("发布草稿: id={}", id);
        return travelNoteService.publishDraft(id);
    }

    @Operation(summary = "更新游记")
    @PutMapping("/{id}")
    public Result updateNote(
            @Parameter(description = "游记ID") @PathVariable("id") Long id,
            @RequestBody TravelNote travelNote) {
        log.debug("更新游记: id={}, travelNote={}", id, travelNote);
        return travelNoteService.updateNote(id, travelNote);
    }

    @Operation(summary = "审核游记")
    @PutMapping("/audit/{id}")
    public Result auditNote(
            @Parameter(description = "游记ID") @PathVariable("id") Long id,
            @Parameter(description = "状态") @RequestParam("status") Integer status) {
        log.debug("审核游记: id={}, status={}", id, status);
        return travelNoteService.auditNote(id, status);
    }

    @Operation(summary = "删除游记")
    @DeleteMapping("/{id}")
    public Result deleteNote(
            @Parameter(description = "游记ID") @PathVariable("id") Long id) {
        log.debug("删除游记: id={}", id);
        return travelNoteService.deleteNote(id);
    }

    @Operation(summary = "获取我的草稿")
    @GetMapping("/drafts")
    public Result getMyDrafts(
            @Parameter(description = "当前页") @RequestParam(value = "current", defaultValue = "1") Integer current) {
        log.debug("获取我的草稿: current={}", current);
        return travelNoteService.getMyDrafts(current);
    }

    @Operation(summary = "点赞游记")
    @PutMapping("/like/{id}")
    public Result likeNote(
            @Parameter(description = "游记ID") @PathVariable("id") Long id) {
        log.debug("点赞游记: id={}", id);
        return travelNoteService.likeNote(id);
    }

    @Operation(summary = "获取我的游记")
    @GetMapping("/my")
    public Result getMyNotes(
            @Parameter(description = "当前页") @RequestParam(value = "current", defaultValue = "1") Integer current) {
        log.debug("获取我的游记: current={}", current);
        return travelNoteService.getMyNotes(current);
    }

    @Operation(summary = "获取热门游记")
    @GetMapping("/hot")
    public Result getHotNotes(
            @Parameter(description = "当前页") @RequestParam(value = "current", defaultValue = "1") Integer current) {
        log.debug("获取热门游记: current={}", current);
        return travelNoteService.getHotNotes(current);
    }

    @Operation(summary = "根据ID查询游记")
    @GetMapping("/{id}")
    public Result getNoteById(
            @Parameter(description = "游记ID") @PathVariable("id") Long id) {
        log.debug("根据ID查询游记: id={}", id);
        return travelNoteService.getNoteById(id);
    }

    @Operation(summary = "获取游记点赞列表")
    @GetMapping("/likes/{id}")
    public Result getLikes(
            @Parameter(description = "游记ID") @PathVariable("id") Long id) {
        log.debug("获取游记点赞列表: id={}", id);
        return travelNoteService.getLikes(id);
    }

    @Operation(summary = "获取用户游记")
    @GetMapping("/user/{userId}")
    public Result getUserNotes(
            @Parameter(description = "用户ID") @PathVariable("userId") Long userId,
            @Parameter(description = "当前页") @RequestParam(value = "current", defaultValue = "1") Integer current) {
        log.debug("获取用户游记: userId={}, current={}", userId, current);
        return travelNoteService.getUserNotes(userId, current);
    }

    @Operation(summary = "获取关注用户游记")
    @GetMapping("/follow")
    public Result getFollowNotes(
            @Parameter(description = "上次时间") @RequestParam(value = "lastTime", required = false) LocalDateTime lastTime,
            @Parameter(description = "上次ID") @RequestParam(value = "lastId", required = false) Long lastId,
            @Parameter(description = "当前页") @RequestParam(value = "current", defaultValue = "1") Integer current) {
        log.debug("获取关注用户游记: lastTime={}, lastId={}, current={}", lastTime, lastId, current);
        return travelNoteService.getFollowNotes(lastTime, lastId, current);
    }

    @Operation(summary = "获取景点游记")
    @GetMapping("/scenic/{scenicId}")
    public Result getNotesByScenic(
            @Parameter(description = "景点ID") @PathVariable("scenicId") Long scenicId,
            @Parameter(description = "当前页") @RequestParam(value = "current", defaultValue = "1") Integer current) {
        log.debug("获取景点游记: scenicId={}, current={}", scenicId, current);
        return travelNoteService.getNotesByScenic(scenicId, current);
    }

    @Operation(summary = "搜索游记")
    @GetMapping("/search")
    public Result searchNotes(
            @Parameter(description = "搜索关键词") @RequestParam("keyword") String keyword,
            @Parameter(description = "当前页") @RequestParam(value = "current", defaultValue = "1") Integer current,
            @Parameter(description = "景点ID") @RequestParam(value = "scenicId", required = false) Long scenicId,
            @Parameter(description = "标签") @RequestParam(value = "tags", required = false) String tags,
            @Parameter(description = "排序方式") @RequestParam(value = "sort", required = false) String sort) {
        log.debug("搜索游记: keyword={}, current={}, scenicId={}, tags={}, sort={}", keyword, current, scenicId, tags, sort);
        return travelNoteService.searchNotes(keyword, current, tags, sort, scenicId);
    }
}