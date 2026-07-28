package com.smarttravel.travel.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smarttravel.common.dto.Result;
import com.smarttravel.common.dto.UserDTO;
import com.smarttravel.common.utils.JwtUtils;
import com.smarttravel.common.utils.RedisConstants;
import com.smarttravel.common.utils.SystemConstants;
import com.smarttravel.common.utils.UserHolder;
import com.smarttravel.travel.entity.NoteImage;
import com.smarttravel.travel.entity.TravelNote;
import com.smarttravel.travel.mapper.NoteImageMapper;
import com.smarttravel.travel.mapper.TravelNoteMapper;
import com.smarttravel.travel.service.ITravelNoteService;
import com.smarttravel.travel.service.TravelNoteEsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TravelNoteServiceImpl extends ServiceImpl<TravelNoteMapper, TravelNote> implements ITravelNoteService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private NoteImageMapper noteImageMapper;

    @Resource
    private TravelNoteEsService travelNoteEsService;

    @Override
    public Result publishNote(TravelNote travelNote) {
        UserDTO user = UserHolder.getUser();
        travelNote.setUserId(user.getId());
        travelNote.setStatus(SystemConstants.BLOG_STATUS_REVIEWING);
        save(travelNote);
        return Result.ok(travelNote);
    }

    @Override
    public Result saveDraft(TravelNote travelNote) {
        UserDTO user = UserHolder.getUser();
        travelNote.setUserId(user.getId());
        travelNote.setStatus(SystemConstants.BLOG_STATUS_DRAFT);
        save(travelNote);
        return Result.ok(travelNote);
    }

    @Override
    public Result publishDraft(Long id) {
        TravelNote note = getById(id);
        if (note == null) {
            return Result.fail("游记不存在");
        }
        note.setStatus(SystemConstants.BLOG_STATUS_REVIEWING);
        updateById(note);
        return Result.ok();
    }

    @Override
    public Result updateNote(Long id, TravelNote travelNote) {
        TravelNote note = getById(id);
        if (note == null) {
            return Result.fail("游记不存在");
        }
        note.setTitle(travelNote.getTitle());
        note.setContent(travelNote.getContent());
        note.setTags(travelNote.getTags());
        note.setScenicId(travelNote.getScenicId());
        note.setStatus(SystemConstants.BLOG_STATUS_REVIEWING);
        updateById(note);
        try {
            travelNoteEsService.deleteNote(id);
        } catch (Exception e) {
            log.error("ES删除游记失败: id={}", id, e);
        }
        return Result.ok();
    }

    @Override
    public Result auditNote(Long id, Integer status) {
        TravelNote note = getById(id);
        if (note == null) {
            return Result.fail("游记不存在");
        }
        note.setStatus(status);
        updateById(note);
        if (status.equals(SystemConstants.BLOG_STATUS_PUBLISHED)) {
            travelNoteEsService.indexNote(note);
        } else if (status.equals(SystemConstants.BLOG_STATUS_DELETED)) {
            travelNoteEsService.deleteNote(id);
        }
        return Result.ok();
    }

    @Override
    public Result getMyDrafts(Integer current) {
        UserDTO user = UserHolder.getUser();
        Page<TravelNote> page = query()
                .eq("user_id", user.getId())
                .eq("status", SystemConstants.BLOG_STATUS_DRAFT)
                .orderByDesc("create_time")
                .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @Override
    public Result likeNote(Long id) {
        TravelNote note = getById(id);
        if (note == null) {
            return Result.fail("游记不存在");
        }
        UserDTO user = UserHolder.getUser();
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, user.getId().toString());
        if (score != null) {
            stringRedisTemplate.opsForZSet().remove(key, user.getId().toString());
            update().setSql("liked = liked - 1").eq("id", id).update();
        } else {
            stringRedisTemplate.opsForZSet().add(key, user.getId().toString(), System.currentTimeMillis());
            update().setSql("liked = liked + 1").eq("id", id).update();
        }
        return Result.ok();
    }

    @Override
    public Result getMyNotes(Integer current) {
        UserDTO user = UserHolder.getUser();
        Page<TravelNote> page = query()
                .eq("user_id", user.getId())
                .eq("status", SystemConstants.BLOG_STATUS_PUBLISHED)
                .orderByDesc("create_time")
                .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @Override
    public Result getHotNotes(Integer current) {
        String key = RedisConstants.BLOG_HOT_KEY;
        Set<String> topIds = stringRedisTemplate.opsForZSet()
                .reverseRange(key, SystemConstants.BLOG_HOT_START, SystemConstants.BLOG_HOT_END);
        if (topIds == null || topIds.isEmpty()) {
            Page<TravelNote> page = query()
                    .eq("status", SystemConstants.BLOG_STATUS_PUBLISHED)
                    .orderByDesc("liked")
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            return Result.ok(page.getRecords(), page.getTotal());
        }
        List<Long> ids = topIds.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",", ids);
        List<TravelNote> notes = query()
                .eq("status", SystemConstants.BLOG_STATUS_PUBLISHED)
                .in("id", ids)
                .last("ORDER BY FIELD(id, " + idStr + ")")
                .list();
        return Result.ok(notes);
    }

    @Override
    public Result getNoteById(Long id) {
        TravelNote note = getById(id);
        if (note == null) {
            return Result.fail("游记不存在");
        }
        UserDTO user = UserHolder.getUser();
        if (user != null) {
            String key = RedisConstants.BLOG_LIKED_KEY + id;
            Double score = stringRedisTemplate.opsForZSet().score(key, user.getId().toString());
            note.setIsLiked(score != null);
        }
        List<NoteImage> images = noteImageMapper.selectList(
                new QueryWrapper<NoteImage>().eq("note_id", id).orderByAsc("sort"));
        note.setImageList(images);
        return Result.ok(note);
    }

    @Override
    public Result getLikes(Long id) {
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        Set<String> topIds = stringRedisTemplate.opsForZSet().reverseRange(key, 0, 4);
        if (topIds == null || topIds.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> ids = topIds.stream().map(Long::valueOf).collect(Collectors.toList());
        List<UserDTO> userDTOs = ids.stream().map(uid -> {
            String userKey = RedisConstants.LOGIN_USER_KEY + uid;
            String token = stringRedisTemplate.opsForValue().get(userKey);
            if (token == null) {
                return null;
            }
            io.jsonwebtoken.Claims claims = JwtUtils.parseToken(token);
            return UserDTO.builder()
                    .id(uid)
                    .nickname(claims.get("nickname", String.class))
                    .icon(claims.get("icon", String.class))
                    .build();
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return Result.ok(userDTOs);
    }

    @Override
    public Result getUserNotes(Long userId, Integer current) {
        Page<TravelNote> page = query()
                .eq("user_id", userId)
                .eq("status", SystemConstants.BLOG_STATUS_PUBLISHED)
                .orderByDesc("create_time")
                .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @Override
    public Result getNotesByScenic(Long scenicId, Integer current) {
        Page<TravelNote> page = query()
                .eq("scenic_id", scenicId)
                .eq("status", SystemConstants.BLOG_STATUS_PUBLISHED)
                .orderByDesc("create_time")
                .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @Override
    public Result searchNotes(String keyword, Integer current, String tags, String sort, Long scenicId) {
        return travelNoteEsService.searchNotes(keyword, current, tags, sort, scenicId);
    }

    @Override
    public Result getFollowNotes(LocalDateTime lastTime, Long lastId, Integer current) {
        UserDTO user = UserHolder.getUser();
        String key = RedisConstants.FOLLOW_KEY + user.getId();
        Set<String> followIds = stringRedisTemplate.opsForSet().members(key);
        if (followIds == null || followIds.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> ids = followIds.stream().map(Long::valueOf).collect(Collectors.toList());
        QueryWrapper<TravelNote> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", ids);
        queryWrapper.eq("status", SystemConstants.BLOG_STATUS_PUBLISHED);
        if (lastTime != null && lastId != null) {
            queryWrapper.and(w -> w
                    .lt("create_time", lastTime)
                    .or()
                    .eq("create_time", lastTime).lt("id", lastId));
        }
        queryWrapper.orderByDesc("create_time", "id");
        Page<TravelNote> page = page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE), queryWrapper);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @Override
    public Result deleteNote(Long id) {
        TravelNote note = getById(id);
        if (note == null) {
            return Result.fail("游记不存在");
        }
        note.setStatus(SystemConstants.BLOG_STATUS_DELETED);
        updateById(note);
        try {
            travelNoteEsService.deleteNote(id);
        } catch (Exception e) {
            log.error("ES删除游记失败: id={}", id, e);
        }
        return Result.ok();
    }
}