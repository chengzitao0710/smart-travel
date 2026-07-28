package com.smarttravel.trajectory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "旅行轨迹")
@TableName("tb_trajectory")
public class Trajectory {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "景点ID")
    private Long scenicId;
    @Schema(description = "到访时间")
    private LocalDateTime visitTime;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}