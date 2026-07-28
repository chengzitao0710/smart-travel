package com.smarttravel.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "签到记录")
@TableName("tb_sign")
public class Sign {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "年份")
    private Integer year;
    @Schema(description = "月份")
    private Integer mouth;
    @Schema(description = "签到日期")
    private LocalDateTime date;
    @Schema(description = "是否补签")
    private Integer isBackup;
}