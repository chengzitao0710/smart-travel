package com.smarttravel.social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smarttravel.common.dto.Result;
import com.smarttravel.common.utils.RedisConstants;
import com.smarttravel.common.utils.UserHolder;
import com.smarttravel.social.entity.Follow;
import com.smarttravel.social.mapper.FollowMapper;
import com.smarttravel.social.service.IFollowService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 关注用户
     * @param followUserId 用户ID
     * @param isFollow 是否关注用户
     * @return 关注结果
     */
    @Override
    @Transactional
    public Result follow(Long followUserId, Boolean isFollow) {
        Long userId = UserHolder.getUser().getId();
        String key = RedisConstants.FOLLOW_KEY + userId;

        if (Boolean.TRUE.equals(isFollow)) {
            Follow follow = Follow.builder()
                    .userId(userId)
                    .followUserId(followUserId)
                    .build();
            save(follow);

            stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            return Result.ok("关注成功");
        } else {
            remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId)
                    .eq("follow_user_id", followUserId));

            stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            return Result.ok("取消关注");
        }
    }

    /**
     * 是否关注用户
     * @param followUserId 用户ID
     * @return 是否关注用户
     */
    @Override
    public Result isFollow(Long followUserId) {
        Long userId = UserHolder.getUser().getId();
        String key = RedisConstants.FOLLOW_KEY + userId;

        Boolean isFollow = stringRedisTemplate.opsForSet().isMember(key, followUserId.toString());
        return Result.ok(Boolean.TRUE.equals(isFollow));
    }

    /**
     * 获取共同关注用户
     * @param followUserId 用户ID
     * @return 共同关注用户列表
     */
    @Override
    public Result getCommonFollow(Long followUserId) {
        Long userId = UserHolder.getUser().getId();
        String key1 = RedisConstants.FOLLOW_KEY + userId;
        String key2 = RedisConstants.FOLLOW_KEY + followUserId;

        Set<String> common = stringRedisTemplate.opsForSet().intersect(key1, key2);
        if (common == null || common.isEmpty()) {
            return Result.ok(List.of());
        }
        return Result.ok(common);
    }

    /**
     * 获取粉丝用户列表
     * @param userId 用户ID
     * @return 粉丝用户列表
     */
    @Override
    public Result getFollowers(Long userId) {
        List<Follow> followers = query().eq("follow_user_id", userId).list();
        return Result.ok(followers);
    }

    /**
     * 获取关注列表
     * @param userId 用户ID
     * @return 关注列表
     */
    @Override
    public Result getFollowings(Long userId) {
        String key = RedisConstants.FOLLOW_KEY + userId;
        Set<String> followings = stringRedisTemplate.opsForSet().members(key);

        if (followings == null || followings.isEmpty()) {
            return Result.ok(List.of());
        }
        return Result.ok(followings);
    }

    /**
     * 获取关注/粉丝计数
     * @param userId 用户ID
     * @return { followingCount, followerCount }
     */
    @Override
    public Result getFollowCounts(Long userId) {
        String key = RedisConstants.FOLLOW_KEY + userId;
        Long followingCount = stringRedisTemplate.opsForSet().size(key);

        Long followerCount = query()
                .eq("follow_user_id", userId)
                .count();

        return Result.ok(Map.of(
                "followingCount", followingCount != null ? followingCount : 0L,
                "followerCount", followerCount
        ));
    }
}