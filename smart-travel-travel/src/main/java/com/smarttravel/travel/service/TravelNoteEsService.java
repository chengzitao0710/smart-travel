package com.smarttravel.travel.service;

import com.smarttravel.common.dto.Result;
import com.smarttravel.travel.entity.TravelNote;

public interface TravelNoteEsService {
    void indexNote(TravelNote travelNote);
    void deleteNote(Long id);
    Result searchNotes(String keyword, Integer current, String tags, String sort, Long scenicId);
}