package com.smarttravel.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smarttravel.common.dto.Result;
import com.smarttravel.travel.entity.NoteImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface INoteImageService extends IService<NoteImage> {
    Result uploadNoteImageByBatch(Long noteId, List<MultipartFile> files);
    Result deleteNoteImageByBatch(Long noteId, List<Long> imageIds);
    Result getImagesByNoteId(Long noteId);
}