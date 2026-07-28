package com.smarttravel.route.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smarttravel.scenic.entity.Scenic;
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
@Schema(description = "路线行程")
@TableName("tb_route_detail")
public class RouteDetail {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "路线ID")
    private Long routeId;
    @Schema(description = "第几天")
    private Integer day;
    @Schema(description = "景点ID")
    private Long scenicId;
    @Schema(description = "当天游览顺序")
    private Integer sort;
    @Schema(description = "游玩说明")
    private String description;
    @TableField(exist = false)
    @Schema(description = "景点信息")
    private Scenic scenic;
    @TableField(exist = false)
    @Schema(description = "到下一景点距离")
    private String distance;
    @TableField(exist = false)
    @Schema(description = "到下一景点耗时")
    private String duration;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}