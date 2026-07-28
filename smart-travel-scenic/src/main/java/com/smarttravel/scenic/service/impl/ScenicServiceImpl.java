package com.smarttravel.scenic.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smarttravel.common.dto.Result;
import com.smarttravel.common.utils.CacheClient;
import com.smarttravel.common.utils.RedisConstants;
import com.smarttravel.common.utils.SystemConstants;
import com.smarttravel.scenic.entity.Scenic;
import com.smarttravel.scenic.mapper.ScenicMapper;
import com.smarttravel.scenic.service.IScenicService;
import com.smarttravel.common.utils.AmapUtil;
import com.smarttravel.scenic.service.ScenicEsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ScenicServiceImpl extends ServiceImpl<ScenicMapper, Scenic> implements IScenicService {

    @Resource
    private CacheClient cacheClient;

    @Resource
    private AmapUtil amapUtil;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ScenicEsService scenicEsService;

    /**
     * 根据ID查询景点
     * @param id 景点ID
     * @return 景点详情
     */
    @Override
    public Result getScenicById(Long id) {
        Scenic scenic = cacheClient.queryWithPassThrough(
                RedisConstants.CACHE_SCENIC_KEY,
                id,
                Scenic.class,
                this::getById,
                RedisConstants.CACHE_SCENIC_TTL,
                TimeUnit.MINUTES
        );

        if (scenic == null) {
            return Result.fail("景点不存在");
        }
        return Result.ok(scenic);
    }

    /**
     * 创建景点，若未传经纬度则通过高德地图根据地址自动获取
     * @param scenic 景点信息
     * @return 创建后的景点信息
     */
    @Override
    public Result createScenic(Scenic scenic) {

        fillLocation(scenic);

        save(scenic);

        cacheClient.set(
                RedisConstants.CACHE_SCENIC_KEY + scenic.getId(),
                scenic,
                RedisConstants.CACHE_SCENIC_TTL,
                TimeUnit.MINUTES);
        saveScenicToGeo(scenic);
        try {
            scenicEsService.indexScenic(scenic);
        } catch (Exception e) {
            log.warn("ES索引景点失败，景点id={}，后续可通过手动同步修复", scenic.getId(), e);
        }
        return Result.ok(scenic);
    }

    /**
     * 更新景点信息
     * @param scenic 景点实体
     * @return 更新后的景点实体
     */
    @Override
    public Result updateScenic(Scenic scenic) {
        if (scenic.getId() == null) {
            return Result.fail("景点ID不能为空");
        }

        fillLocation(scenic);

        if (scenic.getX() == null || scenic.getY() == null) {
            return Result.fail("景点经纬度信息缺失，请检查地址是否正确");
        }

        boolean updated = updateById(scenic);
        if (!updated) {
            return Result.fail("景点不存在或更新失败");
        }

        cacheClient.set(
                RedisConstants.CACHE_SCENIC_KEY + scenic.getId(),
                scenic,
                RedisConstants.CACHE_SCENIC_TTL,
                TimeUnit.MINUTES);
        saveScenicToGeo(scenic);
        try {
            scenicEsService.updateScenic(scenic);
        } catch (Exception e) {
            log.warn("ES更新景点失败，景点id={}，后续可通过手动同步修复", scenic.getId(), e);
        }
        return Result.ok(scenic);
    }

    /**
     * 删除景点
     * @param id 景点id
     * @return 响应结果
     */
    @Override
    public Result deleteScenic(Long id) {
        Scenic scenic = getById(id);
        removeById(id);
        stringRedisTemplate.delete(RedisConstants.CACHE_SCENIC_KEY + id);
        if (scenic != null) {
            removeScenicFromGeo(scenic);
        }
        try {
            scenicEsService.deleteScenic(id);
        } catch (Exception e) {
            log.warn("ES删除景点失败，景点id={}，后续可通过手动同步修复", id, e);
        }
        return Result.ok();
    }

    /**
     * 自动填充景点地理位置信息（经纬度、地址、行政区）
     * @param scenic 景点实体
     */
    public void fillLocation(Scenic scenic) {
        // 1. 已有完整经纬度，直接返回
        if (scenic.getX() != null && scenic.getY() != null) {
            return;
        }

        // 2. 优先通过地址解析坐标
        if (StrUtil.isNotBlank(scenic.getAddress())) {
            fillByAddress(scenic);
            return;
        }

        // 3. 无地址，通过景点名称POI检索补全信息
        if (StrUtil.isNotBlank(scenic.getName())) {
            fillByPoiName(scenic);
        }
    }

    /**
     * 通过地址地理编码填充经纬度
     */
    private void fillByAddress(Scenic scenic) {
        String address = scenic.getAddress();
        double[] lngLat = amapUtil.geocode(address);
        if (lngLat == null) {
            log.warn("地址解析坐标失败，地址：{}", address);
            return;
        }
        scenic.setX(lngLat[0]);
        scenic.setY(lngLat[1]);
        log.info("地址解析坐标成功，地址：{}，经度{}，纬度{}", address, lngLat[0], lngLat[1]);
    }

    /**
     * 通过景点名称POI检索，补全名称/地址/行政区/经纬度
     */
    private void fillByPoiName(Scenic scenic) {
        String scenicName = scenic.getName();
        List<Map<String, String>> poiList = amapUtil.searchPoi(scenicName);
        if (poiList == null || poiList.isEmpty()) {
            log.warn("POI检索无匹配结果，景点名称：{}", scenicName);
            return;
        }

        Map<String, String> firstPoi = poiList.get(0);
        // 补全空字段
        if (StrUtil.isBlank(scenic.getAddress())) {
            scenic.setAddress(firstPoi.get("address"));
        }
        if (StrUtil.isBlank(scenic.getArea())) {
            scenic.setArea(firstPoi.get("area"));
        }
        // 用POI标准名覆盖原始名称
        scenic.setName(firstPoi.get("name"));

        // 解析经纬度，增加异常捕获
        String locationStr = firstPoi.get("location");
        if (StrUtil.isBlank(locationStr)) {
            log.warn("POI无坐标，景点名称：{}", scenicName);
            return;
        }
        try {
            String[] lngLatArr = locationStr.split(",");
            double lng = Double.parseDouble(lngLatArr[0]);
            double lat = Double.parseDouble(lngLatArr[1]);
            scenic.setX(lng);
            scenic.setY(lat);
            log.info("POI检索填充坐标，名称：{}，经度{}，纬度{}", scenicName, lng, lat);
        } catch (NumberFormatException e) {
            log.error("POI坐标转换失败，location={}, name={}", locationStr, scenicName, e);
        }
    }

    /**
     * 按景点名称查询POI列表
     * @param keyword 搜索关键词
     * @return POI列表
     */
    @Override
    public Result searchPoi(String keyword) {
        List<Map<String, String>> pois = amapUtil.searchPoi(keyword);
        return Result.ok(pois);
    }



    @Override
    public Result searchScenic(String keyword, Long typeId, String area, Double x, Double y, String sort, Integer current) {
        if (x != null && y != null) {
            return searchNearby(x, y, typeId, area, sort, current, keyword);
        }

        if (StrUtil.isNotBlank(keyword)) {
            try {
                return scenicEsService.searchScenic(keyword, typeId, area, sort, current);
            } catch (Exception e) {
                log.warn("ES搜索失败，降级为MySQL查询: keyword={}", keyword, e);
            }
        }

        return searchByFilters(typeId, area, sort, current, keyword);
    }

    private Result searchNearby(Double x, Double y, Long typeId, String area, String sort, Integer current, String keyword) {
        String key = typeId != null
                ? RedisConstants.SCENIC_GEO_KEY + typeId
                : RedisConstants.SCENIC_GEO_KEY + "all";

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
                .radius(key,
                        new Circle(new Point(x, y), new Distance(5000)),
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates()
                );

        if (results == null || results.getContent().isEmpty()) {
            return Result.ok(Collections.emptyList(), 0L);
        }

        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> geoList = results.getContent();
        List<Long> ids = new ArrayList<>();
        Map<String, Double> distanceMap = new HashMap<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult : geoList) {
            String scenicIdStr = geoResult.getContent().getName();
            ids.add(Long.valueOf(scenicIdStr));
            distanceMap.put(scenicIdStr, geoResult.getDistance().getValue());
        }

        var query = query().eq("status", SystemConstants.SCENIC_STATUS_ON).in("id", ids);
        if (StrUtil.isNotBlank(area)) {
            query.eq("area", area);
        }
        if (StrUtil.isNotBlank(keyword)) {
            query.like("name", keyword);
        }
        if ("hot".equals(sort)) {
            query.orderByDesc("sold");
        } else if ("top".equals(sort)) {
            query.orderByDesc("score");
        }

        List<Scenic> allScenicList = query.list();
        for (Scenic scenic : allScenicList) {
            scenic.setDistance(distanceMap.getOrDefault(scenic.getId().toString(), 0.0));
        }

        if (!"hot".equals(sort) && !"top".equals(sort)) {
            allScenicList.sort(Comparator.comparingDouble(s ->
                    distanceMap.getOrDefault(s.getId().toString(), Double.MAX_VALUE)));
        }

        long total = allScenicList.size();
        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        if (from >= total) {
            return Result.ok(Collections.emptyList(), total);
        }
        int to = Math.min(from + SystemConstants.DEFAULT_PAGE_SIZE, (int) total);
        List<Scenic> pageList = allScenicList.subList(from, to);

        return Result.ok(pageList, total);
    }

    private Result searchByFilters(Long typeId, String area, String sort, Integer current, String keyword) {
        var query = query().eq("status", SystemConstants.SCENIC_STATUS_ON);
        if (typeId != null) {
            query.eq("type_id", typeId);
        }
        if (StrUtil.isNotBlank(area)) {
            query.eq("area", area);
        }
        if (StrUtil.isNotBlank(keyword)) {
            query.like("name", keyword);
        }
        if ("hot".equals(sort)) {
            query.orderByDesc("sold");
        } else if ("top".equals(sort)) {
            query.orderByDesc("score");
        } else {
            query.orderByDesc("create_time");
        }

        Page<Scenic> page = query.page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
        return Result.ok(page.getRecords(), page.getTotal());
    }

    /**
     * 切换景点上下架状态
     * @param id 景点ID
     * @param status 状态 1-上架 0-下架
     * @return 操作结果成功
     */
    @Override
    public Result toggleStatus(Long id, Integer status) {
        update().set("status", status).eq("id", id).update();
        Scenic scenic = getById(id);
        if (status.equals(SystemConstants.SCENIC_STATUS_ON)) {
            cacheClient.set(
                    RedisConstants.CACHE_SCENIC_KEY + id,
                    scenic,
                    RedisConstants.CACHE_SCENIC_TTL,
                    TimeUnit.MINUTES);
            saveScenicToGeo(scenic);
            try {
                scenicEsService.indexScenic(scenic);
            } catch (Exception e) {
                log.warn("ES索引景点失败，景点id={}，后续可通过手动同步修复", id, e);
            }
        } else {
            stringRedisTemplate.delete(RedisConstants.CACHE_SCENIC_KEY + id);
            if (scenic != null) {
                removeScenicFromGeo(scenic);
            }
            try {
                scenicEsService.deleteScenic(id);
            } catch (Exception e) {
                log.warn("ES删除景点失败，景点id={}，后续可通过手动同步修复", id, e);
            }
        }
        return Result.ok();
    }

    /**
     * 将景点坐标写入 Redis GEO
     */
    private void saveScenicToGeo(Scenic scenic) {
        if (scenic.getTypeId() == null || scenic.getX() == null || scenic.getY() == null) {
            return;
        }
        Point point = new Point(scenic.getX(), scenic.getY());
        String scenicId = scenic.getId().toString();
        stringRedisTemplate.opsForGeo().add(RedisConstants.SCENIC_GEO_KEY + scenic.getTypeId(), point, scenicId);
        stringRedisTemplate.opsForGeo().add(RedisConstants.SCENIC_GEO_KEY + "all", point, scenicId);
    }

    /**
     * 从 Redis GEO 中移除景点坐标
     */
    private void removeScenicFromGeo(Scenic scenic) {
        if (scenic.getTypeId() == null) {
            return;
        }
        String scenicId = scenic.getId().toString();
        stringRedisTemplate.opsForGeo().remove(RedisConstants.SCENIC_GEO_KEY + scenic.getTypeId(), scenicId);
        stringRedisTemplate.opsForGeo().remove(RedisConstants.SCENIC_GEO_KEY + "all", scenicId);
    }

    /**
     * 全量同步所有景点到 Redis GEO
     */
    @Override
    public Result syncAllScenicGeo() {
        List<Scenic> scenicList = list();
        int count = 0;
        for (Scenic scenic : scenicList) {
            if (scenic.getTypeId() != null && scenic.getX() != null && scenic.getY() != null) {
                Point point = new Point(scenic.getX(), scenic.getY());
                String scenicId = scenic.getId().toString();
                stringRedisTemplate.opsForGeo().add(RedisConstants.SCENIC_GEO_KEY + scenic.getTypeId(), point, scenicId);
                stringRedisTemplate.opsForGeo().add(RedisConstants.SCENIC_GEO_KEY + "all", point, scenicId);
                count++;
            }
        }
        log.info("全量同步 Redis GEO 完成，共写入 {} 条景点坐标", count);
        return Result.ok(count);
    }

    @Override
    public void incrementComments(Long scenicId) {
        baseMapper.incrementComments(scenicId);
        Scenic scenic = getById(scenicId);
        if (scenic != null) {
            try {
                scenicEsService.updateScenic(scenic);
            } catch (Exception e) {
                log.warn("ES同步评论数失败: scenicId={}", scenicId, e);
            }
        }
    }
}