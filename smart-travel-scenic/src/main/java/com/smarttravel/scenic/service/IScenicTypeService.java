package com.smarttravel.scenic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smarttravel.common.dto.Result;
import com.smarttravel.scenic.entity.ScenicType;

public interface IScenicTypeService extends IService<ScenicType> {
    Result getAllTypes();
}
