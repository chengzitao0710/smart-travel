package com.smarttravel.common.utils;

import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import com.smarttravel.common.config.OssProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class OssUtils {

    @Resource
    private OSS ossClient;

    @Resource
    private OssProperties ossProperties;

    public String upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectName = datePath + "/" + IdUtil.simpleUUID() + suffix;

        try {
            PutObjectResult result = ossClient.putObject(
                    ossProperties.getBucketName(),
                    objectName,
                    file.getInputStream()
            );
            log.debug("OSS upload success: {}", objectName);
        } catch (IOException e) {
            log.error("OSS upload failed", e);
            throw new RuntimeException("文件上传失败", e);
        }

        return "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/" + objectName;
    }

    public void delete(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        String prefix = "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/";
        if (!url.startsWith(prefix)) {
            log.warn("OSS delete skipped, url not match bucket: {}", url);
            return;
        }
        String objectName = url.substring(prefix.length());
        ossClient.deleteObject(ossProperties.getBucketName(), objectName);
        log.debug("OSS delete success: {}", objectName);
    }
}