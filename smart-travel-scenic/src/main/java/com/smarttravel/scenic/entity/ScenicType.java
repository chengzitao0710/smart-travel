package com.smarttravel.scenic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "景点类型")
@TableName("tb_scenic_type")
public class ScenicType {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "类型名称")
    private String name;
    @Schema(description = "类型图标")
    private String icon;
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}