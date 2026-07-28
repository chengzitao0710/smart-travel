package com.smarttravel.route.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smarttravel.common.dto.Result;
import com.smarttravel.common.utils.AmapUtil;
import com.smarttravel.common.utils.OssUtils;
import com.smarttravel.common.utils.SystemConstants;
import com.smarttravel.common.utils.UserHolder;
import com.smarttravel.route.entity.Route;
import com.smarttravel.route.entity.RouteDetail;
import com.smarttravel.route.mapper.RouteDetailMapper;
import com.smarttravel.route.mapper.RouteMapper;
import com.smarttravel.route.service.IRouteService;
import com.smarttravel.scenic.entity.Scenic;
import com.smarttravel.scenic.mapper.ScenicMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RouteServiceImpl extends ServiceImpl<RouteMapper, Route> implements IRouteService {

    @Resource
    private RouteDetailMapper routeDetailMapper;

    @Resource
    private ScenicMapper scenicMapper;

    @Resource
    private AmapUtil amapUtil;

    @Resource
    private OssUtils ossUtils;

    @Override
    @Transactional
    public Result createRoute(Route route) {
        Long userId = UserHolder.getUser().getId();
        route.setUserId(userId);
        save(route);
        return Result.ok(route);
    }

    @Override
    @Transactional
    public Result updateRoute(Route route) {
        Route exist = getById(route.getId());
        if (exist == null) {
            return Result.fail("路线不存在");
        }
        Long userId = UserHolder.getUser().getId();
        if (!exist.getUserId().equals(userId)) {
            return Result.fail("只能修改自己的路线");
        }
        updateById(route);
        return Result.ok(route);
    }

    @Override
    public Result getRouteById(Long id) {
        Route route = getById(id);
        if (route == null) {
            return Result.fail("路线不存在");
        }
        return Result.ok(route);
    }

    @Override
    public Result getRouteList(Integer current, Integer difficulty, String city) {
        LambdaQueryWrapper<Route> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Route::getStatus, SystemConstants.ROUTE_STATUS_ON)
                .eq(difficulty != null, Route::getDifficulty, difficulty)
                .eq(city != null && !city.isEmpty(), Route::getCity, city)
                .orderByDesc(Route::getIsHot)
                .orderByDesc(Route::getCreateTime);

        Page<Route> page = new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE);
        page = page(page, wrapper);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @Override
    public Result getRouteDetails(Long routeId) {
        Route route = getById(routeId);
        if (route == null) {
            return Result.fail("路线不存在");
        }

        List<RouteDetail> details = routeDetailMapper.selectList(
                new LambdaQueryWrapper<RouteDetail>()
                        .eq(RouteDetail::getRouteId, routeId)
                        .orderByAsc(RouteDetail::getDay)
                        .orderByAsc(RouteDetail::getSort));

        for (RouteDetail detail : details) {
            Scenic scenic = scenicMapper.selectById(detail.getScenicId());
            detail.setScenic(scenic);
        }

        RouteDetail prev = null;
        for (RouteDetail detail : details) {
            if (prev != null
                    && prev.getScenic() != null && prev.getScenic().getX() != null
                    && detail.getScenic() != null && detail.getScenic().getX() != null) {
                Map<String, Object> dir = amapUtil.direction(
                        prev.getScenic().getX(), prev.getScenic().getY(),
                        detail.getScenic().getX(), detail.getScenic().getY());
                if (dir != null) {
                    detail.setDistance((String) dir.get("distance"));
                    detail.setDuration((String) dir.get("duration"));
                }
            }
            prev = detail;
        }

        route.setViewCount(route.getViewCount() != null ? route.getViewCount() + 1 : 1);
        updateById(route);

        Map<String, Object> result = new HashMap<>();
        result.put("route", route);
        result.put("details", details);
        return Result.ok(result);
    }

    @Override
    @Transactional
    public Result deleteRoute(Long id) {
        Route route = getById(id);
        if (route == null) {
            return Result.fail("路线不存在");
        }
        removeById(id);
        routeDetailMapper.delete(new LambdaQueryWrapper<RouteDetail>()
                .eq(RouteDetail::getRouteId, id));
        return Result.ok();
    }

    @Override
    public Result calcDirection(Long routeId) {
        List<RouteDetail> details = routeDetailMapper.selectList(
                new LambdaQueryWrapper<RouteDetail>()
                        .eq(RouteDetail::getRouteId, routeId)
                        .orderByAsc(RouteDetail::getDay)
                        .orderByAsc(RouteDetail::getSort));

        if (details.isEmpty()) {
            return Result.fail("路线没有景点");
        }

        List<Map<String, Object>> segments = new ArrayList<>();
        long totalDistance = 0;
        long totalDuration = 0;

        RouteDetail prev = null;
        for (RouteDetail detail : details) {
            Scenic scenic = scenicMapper.selectById(detail.getScenicId());
            detail.setScenic(scenic);

            if (prev != null
                    && prev.getScenic() != null && prev.getScenic().getX() != null
                    && detail.getScenic() != null && detail.getScenic().getX() != null) {
                Map<String, Object> dir = amapUtil.direction(
                        prev.getScenic().getX(), prev.getScenic().getY(),
                        detail.getScenic().getX(), detail.getScenic().getY());
                if (dir != null) {
                    long dist = Long.parseLong((String) dir.get("distance"));
                    long dura = Long.parseLong((String) dir.get("duration"));
                    totalDistance += dist;
                    totalDuration += dura;

                    Map<String, Object> seg = new HashMap<>();
                    seg.put("from", prev.getScenic().getName());
                    seg.put("to", detail.getScenic().getName());
                    seg.put("distance", dist);
                    seg.put("duration", dura);
                    segments.add(seg);
                }
            }
            prev = detail;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("segments", segments);
        result.put("totalDistance", totalDistance);
        result.put("totalDuration", totalDuration);
        result.put("totalDistanceDesc", String.format("%.1f公里", totalDistance / 1000.0));
        result.put("totalDurationDesc", String.format("%d分钟", totalDuration / 60));
        return Result.ok(result);
    }

    @Override
    public Result getHotRoutes() {
        LambdaQueryWrapper<Route> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Route::getStatus, SystemConstants.ROUTE_STATUS_ON)
                .eq(Route::getIsHot, SystemConstants.IS_HOT_YES)
                .orderByDesc(Route::getViewCount)
                .last("LIMIT 10");
        List<Route> routes = list(wrapper);
        return Result.ok(routes);
    }

    @Override
    public Result getMyRoutes(Integer current) {
        Long userId = UserHolder.getUser().getId();
        LambdaQueryWrapper<Route> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Route::getUserId, userId)
                .orderByDesc(Route::getCreateTime);

        Page<Route> page = new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE);
        page = page(page, wrapper);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional
    public Result saveRouteDetails(Long routeId, List<RouteDetail> details) {
        Route route = getById(routeId);
        if (route == null) {
            return Result.fail("路线不存在");
        }
        routeDetailMapper.delete(new LambdaQueryWrapper<RouteDetail>()
                .eq(RouteDetail::getRouteId, routeId));
        for (RouteDetail detail : details) {
            detail.setRouteId(routeId);
            detail.setId(null);
            routeDetailMapper.insert(detail);
        }
        route.setDays(details.stream()
                .map(RouteDetail::getDay)
                .max(Integer::compareTo)
                .orElse(1));
        updateById(route);
        return Result.ok(details);
    }

    @Override
    @Transactional
    public Result updateRouteDetail(RouteDetail detail) {
        RouteDetail exist = routeDetailMapper.selectById(detail.getId());
        if (exist == null) {
            return Result.fail("行程不存在");
        }
        routeDetailMapper.updateById(detail);
        return Result.ok(detail);
    }

    @Override
    @Transactional
    public Result deleteRouteDetail(Long detailId) {
        RouteDetail detail = routeDetailMapper.selectById(detailId);
        if (detail == null) {
            return Result.fail("行程不存在");
        }
        routeDetailMapper.deleteById(detailId);
        return Result.ok();
    }

    @Override
    public Result uploadCover(Long routeId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }
        Route route = getById(routeId);
        if (route == null) {
            return Result.fail("路线不存在");
        }
        String url = ossUtils.upload(file);
        route.setCoverImage(url);
        updateById(route);
        return Result.ok(url);
    }

    @Override
    public Result setRouteHot(Long routeId, Integer isHot) {
        Route route = getById(routeId);
        if (route == null) {
            return Result.fail("路线不存在");
        }
        route.setIsHot(isHot);
        updateById(route);
        return Result.ok();
    }
}