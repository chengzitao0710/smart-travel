package com.smarttravel.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AmapUtil {

    @Value("${amap.key}")
    private String key;

    private static final String GEO_URL = "https://restapi.amap.com/v3/geocode/geo";
    private static final String POI_URL = "https://restapi.amap.com/v3/place/text";
    private static final String DIRECTION_URL = "https://restapi.amap.com/v3/direction/driving";
    private static final String DISTANCE_URL = "https://restapi.amap.com/v3/distance";

    private static final String STATUS = "1";

    public double[] geocode(String address) {
        if (StrUtil.isBlank(address)) {
            return null;
        }
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("key", key);
            params.put("address", address);
            String result = HttpUtil.get(GEO_URL, params);
            JSONObject json = JSONUtil.parseObj(result);
            if (!STATUS.equals(json.getStr("status"))) {
                log.warn("高德地理编码失败: {}", json.getStr("info"));
                return null;
            }
            JSONArray geocodes = json.getJSONArray("geocodes");
            if (geocodes == null || geocodes.isEmpty()) {
                return null;
            }
            String location = geocodes.getJSONObject(0).getStr("location");
            if (StrUtil.isBlank(location)) {
                return null;
            }
            String[] parts = location.split(",");
            return new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
        } catch (Exception e) {
            log.error("高德地理编码异常: {}", e.getMessage());
            return null;
        }
    }

    public List<Map<String, String>> searchPoi(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return null;
        }
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("key", key);
            params.put("keywords", keyword);
            params.put("types", "110000|120000|140000|060000|080000|100000");
            params.put("offset", "10");
            params.put("extensions", "base");
            String result = HttpUtil.get(POI_URL, params);
            JSONObject json = JSONUtil.parseObj(result);
            if (!STATUS.equals(json.getStr("status"))) {
                log.warn("高德POI搜索失败: {}", json.getStr("info"));
                return null;
            }
            JSONArray pois = json.getJSONArray("pois");
            if (pois == null || pois.isEmpty()) {
                return null;
            }
            List<Map<String, String>> list = new ArrayList<>();
            for (int i = 0; i < pois.size(); i++) {
                JSONObject poi = pois.getJSONObject(i);
                Map<String, String> item = new HashMap<>();
                item.put("name", poi.getStr("name"));
                item.put("address", poi.getStr("address"));
                item.put("area", poi.getStr("adname"));
                item.put("type", poi.getStr("type"));
                String location = poi.getStr("location");
                if (StrUtil.isNotBlank(location)) {
                    item.put("location", location);
                }
                list.add(item);
            }
            return list;
        } catch (Exception e) {
            log.error("高德POI搜索异常: {}", e.getMessage());
            return null;
        }
    }

    public Map<String, Object> direction(double originX, double originY, double destX, double destY) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("key", key);
            params.put("origin", originX + "," + originY);
            params.put("destination", destX + "," + destY);
            params.put("extensions", "base");
            params.put("strategy", "0");
            String result = HttpUtil.get(DIRECTION_URL, params);
            JSONObject json = JSONUtil.parseObj(result);
            if (!STATUS.equals(json.getStr("status"))) {
                log.warn("高德路径规划失败: {}", json.getStr("info"));
                return null;
            }
            JSONObject route = json.getJSONObject("route");
            if (route == null) {
                return null;
            }
            JSONArray paths = route.getJSONArray("paths");
            if (paths == null || paths.isEmpty()) {
                return null;
            }
            JSONObject path = paths.getJSONObject(0);
            Map<String, Object> info = new HashMap<>();
            info.put("distance", path.getStr("distance"));
            info.put("duration", path.getStr("duration"));
            info.put("strategy", path.getStr("strategy"));
            return info;
        } catch (Exception e) {
            log.error("高德路径规划异常: {}", e.getMessage());
            return null;
        }
    }

    public List<Map<String, Object>> distance(String origin, String destinations) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("key", key);
            params.put("origins", origin);
            params.put("destination", destinations);
            params.put("type", "1");
            String result = HttpUtil.get(DISTANCE_URL, params);
            JSONObject json = JSONUtil.parseObj(result);
            if (!STATUS.equals(json.getStr("status"))) {
                log.warn("高德距离测量失败: {}", json.getStr("info"));
                return null;
            }
            JSONArray results = json.getJSONArray("results");
            if (results == null || results.isEmpty()) {
                return null;
            }
            List<Map<String, Object>> list = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                JSONObject item = results.getJSONObject(i);
                Map<String, Object> dist = new HashMap<>();
                dist.put("distance", item.getStr("distance"));
                dist.put("duration", item.getStr("duration"));
                list.add(dist);
            }
            return list;
        } catch (Exception e) {
            log.error("高德距离测量异常: {}", e.getMessage());
            return null;
        }
    }
}