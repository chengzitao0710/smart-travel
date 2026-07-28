package com.smarttravel.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smarttravel.common.dto.Result;
import com.smarttravel.travel.entity.NoteComment;

public interface INoteCommentService extends IService<NoteComment> {
    Result addComment(Long noteId, String content);
    Result replyComment(Long noteId, Long parentId, Long answerId, String content);
    Result getComments(Long noteId, Integer current);
    Result likeComment(Long id);
    Result deleteComment(Long id);
    Result reportComment(Long id);
    Result banComment(Long id);
}