package com.smarttravel.travel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smarttravel.common.dto.Result;
import com.smarttravel.common.utils.OssUtils;
import com.smarttravel.travel.entity.NoteImage;
import com.smarttravel.travel.mapper.NoteImageMapper;
import com.smarttravel.travel.service.INoteImageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NoteImageServiceImpl extends ServiceImpl<NoteImageMapper, NoteImage> implements INoteImageService {

    @Resource
    private OssUtils ossUtils;

    @Override
    public Result uploadNoteImageByBatch(Long noteId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Result.fail("文件列表不能为空");
        }
        AtomicInteger sort = new AtomicInteger(0);
        List<NoteImage> images = files.stream()
                .map(file -> {
                    String url = ossUtils.upload(file);
                    return NoteImage.builder()
                            .noteId(noteId)
                            .imageUrl(url)
                            .sort(sort.getAndIncrement())
                            .createTime(LocalDateTime.now())
                            .build();
                })
                .collect(Collectors.toList());
        saveBatch(images);
        return Result.ok(images);
    }

    @Override
    public Result deleteNoteImageByBatch(Long noteId, List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return Result.fail("图片ID列表不能为空");
        }
        List<NoteImage> images = listByIds(imageIds);
        for (NoteImage image : images) {
            ossUtils.delete(image.getImageUrl());
        }
        removeByIds(imageIds);
        return Result.ok();
    }

    @Override
    public Result getImagesByNoteId(Long noteId) {
        List<NoteImage> images = query()
                .eq("note_id", noteId)
                .orderByAsc("sort")
                .list();
        return Result.ok(images);
    }
}