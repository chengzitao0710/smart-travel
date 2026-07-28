package com.smarttravel.route.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "旅行路线")
@TableName("tb_travel_route")
public class Route {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "用户ID(0=系统推荐)")
    private Long userId;
    @Schema(description = "路线标题")
    private String title;
    @Schema(description = "目的地城市")
    private String city;
    @Schema(description = "游玩天数")
    private Integer days;
    @Schema(description = "预算(分)")
    private Long budget;
    @Schema(description = "难度 1=轻松 2=适中 3=困难")
    private Integer difficulty;
    @Schema(description = "封面图URL")
    private String coverImage;
    @Schema(description = "路线描述")
    private String description;
    @Schema(description = "标签(逗号分隔)")
    private String tags;
    @Schema(description = "浏览次数")
    private Integer viewCount;
    @Schema(description = "收藏次数")
    private Integer collectCount;
    @Schema(description = "状态 0=下架 1=上架")
    private Integer status;
    @Schema(description = "是否热门 0=否 1=是")
    private Integer isHot;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}