package com.smarttravel.scenic.service;

import com.smarttravel.common.dto.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IScenicImageService {

    Result batchUploadImages(Long scenicId, List<MultipartFile> files);

    Result batchDeleteImage(List<Long> ids);

    Result getImages(Long scenicId);
}