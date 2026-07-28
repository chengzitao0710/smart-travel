package com.smarttravel.scenic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smarttravel.common.dto.Result;
import com.smarttravel.scenic.entity.Scenic;

public interface IScenicService extends IService<Scenic> {
    Result getScenicById(Long id);
    Result createScenic(Scenic scenic);
    Result updateScenic(Scenic scenic);
    Result deleteScenic(Long id);
    Result searchScenic(String keyword, Long typeId, String area, Double x, Double y, String sort, Integer current);
    Result searchPoi(String keyword);
    Result toggleStatus(Long id, Integer status);
    Result syncAllScenicGeo();

    void incrementComments(Long scenicId);
}