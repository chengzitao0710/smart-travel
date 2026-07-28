package com.smarttravel.scenic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarttravel.common.dto.Result;
import com.smarttravel.common.utils.OssUtils;
import com.smarttravel.scenic.entity.ScenicImage;
import com.smarttravel.scenic.mapper.ScenicImageMapper;
import com.smarttravel.scenic.service.IScenicImageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ScenicImageServiceImpl implements IScenicImageService {

    @Resource
    private OssUtils ossUtils;

    @Resource
    private ScenicImageMapper scenicImageMapper;

    @Override
    public Result batchUploadImages(Long scenicId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Result.fail("文件列表不能为空");
        }
        if (scenicId == null) {
            return Result.fail("景点ID不能为空");
        }

        List<String> urls = new ArrayList<>();
        int sort = 0;
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            String url = ossUtils.upload(file);
            urls.add(url);

            ScenicImage scenicImage = ScenicImage.builder()
                    .scenicId(scenicId)
                    .imageUrl(url)
                    .sort(sort)
                    .isCover(sort == 0 ? 1 : 0)
                    .build();
            scenicImageMapper.insert(scenicImage);
            sort++;
        }

        log.info("批量上传景点图片完成，景点ID：{}，上传数量：{}", scenicId, urls.size());
        return Result.ok(urls);
    }

    @Override
    public Result batchDeleteImage(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail("图片ID列表不能为空");
        }

        List<ScenicImage> images = scenicImageMapper.selectBatchIds(ids);
        if (images == null || images.isEmpty()) {
            return Result.fail("图片不存在");
        }

        for (ScenicImage image : images) {
            String imageUrl = image.getImageUrl();
            ossUtils.delete(imageUrl);
            scenicImageMapper.deleteById(image.getId());
        }

        return Result.ok();
    }

    @Override
    public Result getImages(Long scenicId) {
        if (scenicId == null) {
            return Result.fail("景点ID不能为空");
        }

        List<ScenicImage> images = scenicImageMapper.selectList(new LambdaQueryWrapper<ScenicImage>()
                .eq(ScenicImage::getScenicId, scenicId));
        if (images == null || images.isEmpty()) {
            return Result.fail("图片不存在");
        }

        return Result.ok(images);
    }
}