package com.smarttravel.social.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smarttravel.common.dto.Result;
import com.smarttravel.social.entity.Follow;

public interface IFollowService extends IService<Follow> {
    Result follow(Long followUserId, Boolean isFollow);
    Result isFollow(Long followUserId);
    Result getCommonFollow(Long followUserId);
    Result getFollowers(Long userId);
    Result getFollowings(Long userId);
    Result getFollowCounts(Long userId);
}