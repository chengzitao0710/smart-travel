package com.smarttravel.social.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smarttravel.common.dto.Result;
import com.smarttravel.social.entity.Collect;

public interface ICollectService extends IService<Collect> {
    Result collect(Long targetId, Integer targetType);
    Result isCollect(Long targetId, Integer targetType);
    Result getCollections(Integer targetType, Integer current);
}
