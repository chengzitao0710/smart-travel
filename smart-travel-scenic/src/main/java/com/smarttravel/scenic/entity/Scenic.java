package com.smarttravel.scenic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "景点")
@TableName("tb_scenic")
public class Scenic {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "景点名称")
    private String name;
    @Schema(description = "景点类型ID")
    private Long typeId;
    @Schema(description = "图片列表(JSON)")
    private String images;
    @Schema(description = "所属区域")
    private String area;
    @Schema(description = "详细地址")
    private String address;
    @Schema(description = "经度")
    private Double x;
    @Schema(description = "纬度")
    private Double y;
    @TableField(exist = false)
    @Schema(description = "距离(米)")
    private double distance;
    @Schema(description = "人均消费(分)")
    private Long avgPrice;
    @Schema(description = "销量")
    private Integer sold;
    @Schema(description = "评论数")
    private Integer comments;
    @Schema(description = "评分")
    private Integer score;
    @Schema(description = "开放时间")
    private String openHours;
    @Schema(description = "景点描述")
    private String description;
    @Schema(description = "标签(逗号分隔)")
    private String tags;
    @Schema(description = "状态 0=下架 1=上架")
    private Integer status;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}