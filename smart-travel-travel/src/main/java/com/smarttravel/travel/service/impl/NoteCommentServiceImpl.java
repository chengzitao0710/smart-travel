package com.smarttravel.travel.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smarttravel.common.dto.Result;
import com.smarttravel.common.dto.UserDTO;
import com.smarttravel.common.utils.RedisConstants;
import com.smarttravel.common.utils.SystemConstants;
import com.smarttravel.common.utils.UserHolder;
import com.smarttravel.scenic.service.IScenicService;
import com.smarttravel.travel.entity.NoteComment;
import com.smarttravel.travel.entity.TravelNote;
import com.smarttravel.travel.mapper.NoteCommentMapper;
import com.smarttravel.travel.mapper.TravelNoteMapper;
import com.smarttravel.travel.service.INoteCommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NoteCommentServiceImpl extends ServiceImpl<NoteCommentMapper, NoteComment> implements INoteCommentService {

    @Resource
    private TravelNoteMapper travelNoteMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IScenicService scenicService;

    @Override
    public Result addComment(Long noteId, String content) {
        UserDTO user = UserHolder.getUser();
        TravelNote note = travelNoteMapper.selectById(noteId);
        if (note == null) {
            return Result.fail("游记不存在");
        }
        NoteComment comment = NoteComment.builder()
                .userId(user.getId())
                .noteId(noteId)
                .content(content)
                .liked(SystemConstants.COMMENT_DEFAULT_LIKED)
                .status(SystemConstants.COMMENT_STATUS_NORMAL)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        save(comment);
        note.setComments(note.getComments() == null ? 1 : note.getComments() + 1);
        travelNoteMapper.updateById(note);
        scenicService.incrementComments(note.getScenicId());
        return Result.ok(comment.getId());
    }

    @Override
    public Result replyComment(Long noteId, Long parentId, Long answerId, String content) {
        UserDTO user = UserHolder.getUser();
        NoteComment parent = getById(parentId);
        if (parent == null) {
            return Result.fail("父评论不存在");
        }
        NoteComment comment = NoteComment.builder()
                .userId(user.getId())
                .noteId(noteId)
                .parentId(parentId)
                .answerId(answerId)
                .content(content)
                .liked(SystemConstants.COMMENT_DEFAULT_LIKED)
                .status(SystemConstants.COMMENT_STATUS_NORMAL)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        save(comment);
        return Result.ok(comment.getId());
    }

    @Override
    public Result getComments(Long noteId, Integer current) {
        UserDTO user = UserHolder.getUser();
        List<NoteComment> rootComments = query()
                .eq("note_id", noteId)
                .eq("parent_id", SystemConstants.COMMENT_ROOT_PARENT_ID)
                .orderByDesc("create_time")
                .page(new Page<>(current, SystemConstants.COMMENT_PAGE_SIZE))
                .getRecords();
        if (rootComments.isEmpty()) {
            return Result.ok();
        }
        List<Long> rootIds = rootComments.stream().map(NoteComment::getId).collect(Collectors.toList());
        List<NoteComment> replies = query()
                .eq("note_id", noteId)
                .in("parent_id", rootIds)
                .orderByAsc("create_time")
                .list();
        Map<Long, List<NoteComment>> replyMap = replies.stream()
                .collect(Collectors.groupingBy(NoteComment::getParentId));
        for (NoteComment comment : rootComments) {
            comment.setReplies(replyMap.getOrDefault(comment.getId(), List.of()));
            if (user != null) {
                String key = RedisConstants.COMMENT_LIKED_KEY + comment.getId();
                Double score = stringRedisTemplate.opsForZSet().score(key, user.getId().toString());
                comment.setIsLiked(score != null);
            }
        }
        return Result.ok(rootComments);
    }

    @Override
    public Result likeComment(Long id) {
        NoteComment comment = getById(id);
        if (comment == null) {
            return Result.fail("评论不存在");
        }
        UserDTO user = UserHolder.getUser();
        String key = RedisConstants.COMMENT_LIKED_KEY + id;
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
    public Result deleteComment(Long id) {
        NoteComment comment = getById(id);
        if (comment == null) {
            return Result.fail("评论不存在");
        }
        removeById(id);
        return Result.ok();
    }

    @Override
    public Result reportComment(Long id) {
        NoteComment comment = getById(id);
        if (comment == null) {
            return Result.fail("评论不存在");
        }
        comment.setStatus(SystemConstants.COMMENT_STATUS_REPORTED);
        updateById(comment);
        return Result.ok();
    }

    @Override
    public Result banComment(Long id) {
        NoteComment comment = getById(id);
        if (comment == null) {
            return Result.fail("评论不存在");
        }
        comment.setStatus(SystemConstants.COMMENT_STATUS_BANNED);
        updateById(comment);
        return Result.ok();
    }
}