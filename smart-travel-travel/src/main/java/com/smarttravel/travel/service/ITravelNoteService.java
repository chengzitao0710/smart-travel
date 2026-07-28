package com.smarttravel.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smarttravel.common.dto.Result;
import com.smarttravel.travel.entity.TravelNote;

import java.time.LocalDateTime;

public interface ITravelNoteService extends IService<TravelNote> {
    Result publishNote(TravelNote travelNote);
    Result saveDraft(TravelNote travelNote);
    Result publishDraft(Long id);
    Result updateNote(Long id, TravelNote travelNote);
    Result auditNote(Long id, Integer status);
    Result getMyDrafts(Integer current);
    Result likeNote(Long id);
    Result getMyNotes(Integer current);
    Result getHotNotes(Integer current);
    Result getNoteById(Long id);
    Result getLikes(Long id);
    Result getUserNotes(Long userId, Integer current);
    Result getNotesByScenic(Long scenicId, Integer current);
    Result searchNotes(String keyword, Integer current, String tags, String sort, Long scenicId);
    Result getFollowNotes(LocalDateTime lastTime, Long lastId, Integer current);
    Result deleteNote(Long id);
}