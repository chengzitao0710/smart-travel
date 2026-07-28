package com.smarttravel.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "门票")
@TableName("tb_ticket")
public class Ticket {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "景点ID")
    private Long scenicId;
    @Schema(description = "门票标题")
    private String title;
    @Schema(description = "副标题")
    private String subTitle;
    @Schema(description = "使用规则")
    private String rules;
    @Schema(description = "支付金额(分)")
    private Long payValue;
    @Schema(description = "实际价值(分)")
    private Long actualValue;
    @Schema(description = "类型 0=普通 1=秒杀")
    private Integer type;
    @Schema(description = "状态 0=下架 1=上架")
    private Integer status;
    @Schema(description = "库存")
    private Integer stock;
    @Schema(description = "有效期开始")
    private LocalDateTime validStart;
    @Schema(description = "有效期结束")
    private LocalDateTime validEnd;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}