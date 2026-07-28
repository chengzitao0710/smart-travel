package com.smarttravel.scenic.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smarttravel.common.dto.Result;
import com.smarttravel.common.utils.RedisConstants;
import com.smarttravel.scenic.entity.ScenicType;
import com.smarttravel.scenic.mapper.ScenicTypeMapper;
import com.smarttravel.scenic.service.IScenicTypeService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScenicTypeServiceImpl extends ServiceImpl<ScenicTypeMapper, ScenicType> implements IScenicTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 获取所有景点类型
     * @return 所有景点类型列表
     */
    @Override
    public Result getAllTypes() {
        // 从缓存中获取景点类型列表
        String cacheJson = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SCENIC_TYPE_KEY);
        List<ScenicType> types;
        // 1. 判断redis是否有数据
        if(StrUtil.isNotBlank(cacheJson)) {
            types = JSONUtil.toList(cacheJson, ScenicType.class);
            return Result.ok(types);
        }
        // 2. 从数据库中查询景点类型列表
        List<ScenicType> list = query().orderByAsc("sort").list();
        String json = JSONUtil.toJsonStr(list);
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SCENIC_TYPE_KEY, json);
        // 3. 返回景点类型列表
        return Result.ok(list);
    }
}
