package com.smarttravel.route.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smarttravel.common.dto.Result;
import com.smarttravel.route.entity.Route;
import com.smarttravel.route.entity.RouteDetail;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IRouteService extends IService<Route> {
    Result createRoute(Route route);
    Result updateRoute(Route route);
    Result getRouteById(Long id);
    Result getRouteList(Integer current, Integer difficulty, String city);
    Result getRouteDetails(Long routeId);
    Result deleteRoute(Long id);
    Result calcDirection(Long routeId);
    Result getHotRoutes();
    Result getMyRoutes(Integer current);
    Result saveRouteDetails(Long routeId, List<RouteDetail> details);
    Result updateRouteDetail(RouteDetail detail);
    Result deleteRouteDetail(Long detailId);
    Result uploadCover(Long routeId, MultipartFile file);
    Result setRouteHot(Long routeId, Integer isHot);
}