package com.smarttravel.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smarttravel.common.dto.Result;
import com.smarttravel.common.utils.SystemConstants;
import com.smarttravel.common.utils.UserHolder;
import com.smarttravel.social.entity.Collect;
import com.smarttravel.social.mapper.CollectMapper;
import com.smarttravel.social.service.ICollectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements ICollectService {
    /**
     * 收藏目标
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 收藏结果结果
     */
    @Override
    @Transactional
    public Result collect(Long targetId, Integer targetType) {
        Long userId = UserHolder.getUser().getId();

        Collect exist = query()
                .eq("user_id", userId)
                .eq("target_id", targetId)
                .eq("target_type", targetType)
                .one();
        if (exist != null) {
            remove(new QueryWrapper<Collect>()
                    .eq("user_id", userId)
                    .eq("target_id", targetId)
                    .eq("target_type", targetType));
            return Result.ok("取消收藏");
        }
        Collect collect = Collect.builder()
                .userId(userId)
                .targetId(targetId)
                .targetType(targetType)
                .build();
        save(collect);
        return Result.ok("收藏成功");
    }

    /**
     * 判断是否收藏目标
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 判断结果结果
     */
    @Override
    public Result isCollect(Long targetId, Integer targetType) {
        Long userId = UserHolder.getUser().getId();

        Collect exist = query()
                .eq("user_id", userId)
                .eq("target_id", targetId)
                .eq("target_type", targetType)
                .one();

        return Result.ok(exist != null);
    }

    /**
     * 获取收藏目标
     * @param targetType 目标类型
     * @param current 当前页
     * @return 收藏目标结果结果
     */
    @Override
    public Result getCollections(Integer targetType, Integer current) {
        Long userId = UserHolder.getUser().getId();

        Page<Collect> page = query()
                .eq("user_id", userId)
                .eq(targetType != null, "target_type", targetType)
                .orderByDesc("create_time")
                .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));

        return Result.ok(page.getRecords(), page.getTotal());
    }
}
