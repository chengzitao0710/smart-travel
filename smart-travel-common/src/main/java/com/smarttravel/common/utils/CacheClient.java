package com.smarttravel.common.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.smarttravel.common.entity.RedisData;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class CacheClient {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(value), time, unit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(redisData), time, unit);
    }

    public <R, ID> R queryWithLogicalExpire(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isBlank(json)) {
            return null;
        }

        RedisData redisData = JSON.parseObject(json, RedisData.class);
        R r = JSON.parseObject(redisData.getData().toString(), type);

        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            return r;
        }

        String lockKey = "lock:rebuild:" + id;
        boolean isLock = tryLock(lockKey);
        if (isLock) {
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    R newR = dbFallback.apply(id);
                    this.setWithLogicalExpire(key, newR, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    unlock(lockKey);
                }
            });
        }
        return r;
    }

    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json;
        try {
            json = stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            // Redis故障降级，直接查库
            return dbFallback.apply(id);
        }

        // 缓存命中且不为空
        if (StrUtil.isNotBlank(json)) {
            try {
                return JSON.parseObject(json, type);
            } catch (Exception e) {
                // JSON解析异常，删除脏缓存，走数据库
                stringRedisTemplate.delete(key);
            }
        }

        // 缓存未命中，查询数据库
        R data = dbFallback.apply(id);
        if (data == null) {
            // 【防穿透】空值缓存5分钟，拦截重复无效请求
            stringRedisTemplate.opsForValue().set(key, "", 5, TimeUnit.MINUTES);
            return null;
        }

        // 【防雪崩】过期时间随机增加0~30分钟偏移
        long baseMs = unit.toMillis(time);
        long randomMs = RandomUtil.randomLong(0, 1800000);
        long totalExpireMs = baseMs + randomMs;

        // 写入缓存
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(data), totalExpireMs, TimeUnit.MILLISECONDS);
        return data;
    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(flag);
    }

    public void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}