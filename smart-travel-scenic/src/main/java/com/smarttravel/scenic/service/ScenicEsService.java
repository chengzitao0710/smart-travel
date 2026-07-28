package com.smarttravel.scenic.service;

import com.smarttravel.common.dto.Result;
import com.smarttravel.scenic.entity.Scenic;

public interface ScenicEsService {
    void indexScenic(Scenic scenic);
    void deleteScenic(Long id);
    void updateScenic(Scenic scenic);
    Result searchScenic(String keyword, Long typeId, String area, String sort, Integer current);
}